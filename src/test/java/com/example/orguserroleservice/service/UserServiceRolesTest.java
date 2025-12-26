package com.example.orguserroleservice.service;

import com.example.orguserroleservice.dto.UserResponse;
import com.example.orguserroleservice.entity.OrganizationEntity;
import com.example.orguserroleservice.entity.RoleEntity;
import com.example.orguserroleservice.entity.UserEntity;
import com.example.orguserroleservice.repository.OrganizationRepository;
import com.example.orguserroleservice.repository.RoleRepository;
import com.example.orguserroleservice.repository.UserRepository;
import com.example.orguserroleservice.security.OrgUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceRolesTest {

    @Mock UserRepository userRepo;
    @Mock OrganizationRepository orgRepo;
    @Mock RoleRepository roleRepo;
    @Mock PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void setup() {
        service = new UserService(userRepo, orgRepo, roleRepo, passwordEncoder);
    }

    private static void setId(Object entity, Long id) {
        try {
            Field f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void assignRole_addsRole_whenSameOrganization() {
        Long orgId = 10L;

        OrgUserDetails admin = mock(OrgUserDetails.class);
        when(admin.getOrganizationId()).thenReturn(orgId);

        OrganizationEntity org = new OrganizationEntity();
        setId(org, orgId);

        UserEntity target = new UserEntity();
        setId(target, 50L);
        target.setOrganization(org);

        when(userRepo.findByIdWithAuth(50L)).thenReturn(Optional.of(target));

        RoleEntity role = new RoleEntity();
        setId(role, 7L);
        role.setName("ORG_MANAGER");
        role.setOrganization(org);

        when(roleRepo.findByOrganization_IdAndName(orgId, "ORG_MANAGER")).thenReturn(Optional.of(role));

        UserResponse res = service.assignRole(admin, 50L, "ORG_MANAGER");

        assertTrue(res.getRoles().contains("ORG_MANAGER"));
        assertTrue(target.getRoles().stream().anyMatch(r -> "ORG_MANAGER".equals(r.getName())));
    }

    @Test
    void revokeRole_removesRoleById() {
        Long orgId = 10L;

        OrgUserDetails admin = mock(OrgUserDetails.class);
        when(admin.getOrganizationId()).thenReturn(orgId);

        OrganizationEntity org = new OrganizationEntity();
        setId(org, orgId);

        RoleEntity existing = new RoleEntity();
        setId(existing, 7L);
        existing.setName("ORG_MANAGER");
        existing.setOrganization(org);

        UserEntity target = new UserEntity();
        setId(target, 50L);
        target.setOrganization(org);
        target.getRoles().add(existing);

        when(userRepo.findByIdWithAuth(50L)).thenReturn(Optional.of(target));

        RoleEntity roleToRevoke = new RoleEntity();
        setId(roleToRevoke, 7L);
        roleToRevoke.setName("ORG_MANAGER");
        roleToRevoke.setOrganization(org);

        when(roleRepo.findByOrganization_IdAndName(orgId, "ORG_MANAGER")).thenReturn(Optional.of(roleToRevoke));

        UserResponse res = service.revokeRole(admin, 50L, "ORG_MANAGER");

        assertFalse(res.getRoles().contains("ORG_MANAGER"));
        assertTrue(target.getRoles().isEmpty());
    }

    @Test
    void assignRole_throwsNotFound_whenRoleDoesNotExist() {
        Long orgId = 10L;

        OrgUserDetails admin = mock(OrgUserDetails.class);
        when(admin.getOrganizationId()).thenReturn(orgId);

        OrganizationEntity org = new OrganizationEntity();
        setId(org, orgId);

        UserEntity target = new UserEntity();
        setId(target, 50L);
        target.setOrganization(org);

        when(userRepo.findByIdWithAuth(50L)).thenReturn(Optional.of(target));
        when(roleRepo.findByOrganization_IdAndName(orgId, "MISSING_ROLE")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.assignRole(admin, 50L, "MISSING_ROLE"));

        assertEquals(404, ex.getStatusCode().value());
    }
}
