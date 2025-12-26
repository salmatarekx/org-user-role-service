package com.example.orguserroleservice.service;

import com.example.orguserroleservice.dto.UserCreateRequest;
import com.example.orguserroleservice.dto.UserResponse;
import com.example.orguserroleservice.dto.UserUpdateRequest;
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
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceCoreTest {

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
    void createUserByAdmin_createsUserInSameOrg_andAssignsDefaultRole() {
        Long orgId = 10L;

        OrgUserDetails admin = mock(OrgUserDetails.class);
        when(admin.getOrganizationId()).thenReturn(orgId);

        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("user@acme.com");
        req.setDisplayName("User One");
        req.setPassword("pass123");
        req.setOrganizationId(orgId);

        when(userRepo.existsByEmailIgnoreCase("user@acme.com")).thenReturn(false);

        OrganizationEntity org = new OrganizationEntity();
        setId(org, orgId);
        when(orgRepo.findById(orgId)).thenReturn(Optional.of(org));

        RoleEntity orgUser = new RoleEntity();
        setId(orgUser, 7L);
        orgUser.setName("ORG_USER");
        orgUser.setOrganization(org);
        when(roleRepo.findByOrganization_IdAndName(orgId, "ORG_USER")).thenReturn(Optional.of(orgUser));

        when(passwordEncoder.encode("pass123")).thenReturn("HASHED");

        // simulate "save generates id"
        when(userRepo.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            setId(u, 99L);
            return u;
        });

        UserResponse res = service.createUserByAdmin(admin, req);

        assertEquals(99L, res.getId());
        assertEquals(orgId, res.getOrganizationId());
        assertTrue(res.getRoles().contains("ORG_USER"));

        // verify the important effects
        ArgumentCaptor<UserEntity> cap = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepo).save(cap.capture());

        UserEntity saved = cap.getValue();
        assertEquals("user@acme.com", saved.getEmail());
        assertEquals("User One", saved.getDisplayName());
        assertEquals("HASHED", saved.getPasswordHash());
        assertEquals(orgId, saved.getOrganization().getId());
        assertTrue(saved.getRoles().stream().anyMatch(r -> "ORG_USER".equals(r.getName())));
    }

    @Test
    void createUserByAdmin_rejectsDifferentOrganization() {
        Long adminOrg = 10L;

        OrgUserDetails admin = mock(OrgUserDetails.class);
        when(admin.getOrganizationId()).thenReturn(adminOrg);

        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("user@acme.com");
        req.setOrganizationId(99L);

        when(userRepo.existsByEmailIgnoreCase("user@acme.com")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createUserByAdmin(admin, req));

        assertEquals(403, ex.getStatusCode().value());
        verify(userRepo, never()).save(any());
        verifyNoInteractions(orgRepo, roleRepo, passwordEncoder);
    }

    @Test
    void listUsersForOrg_returnsPage_andMapsBasicResponse() {
        Long orgId = 10L;

        OrgUserDetails me = mock(OrgUserDetails.class);
        when(me.getOrganizationId()).thenReturn(orgId);

        OrganizationEntity org = new OrganizationEntity();
        setId(org, orgId);

        UserEntity u = new UserEntity();
        setId(u, 5L);
        u.setEmail("u@acme.com");
        u.setDisplayName("U");
        u.setPasswordHash("x");
        u.setOrganization(org);

        Page<UserEntity> page = new PageImpl<>(List.of(u), PageRequest.of(0, 10), 1);
        when(userRepo.findAllByOrganizationId(eq(orgId), any(Pageable.class))).thenReturn(page);

        Page<UserResponse> res = service.listUsersForOrg(me, PageRequest.of(0, 10));

        assertEquals(1, res.getTotalElements());
        assertEquals("u@acme.com", res.getContent().get(0).getEmail());
        assertTrue(res.getContent().get(0).getRoles().isEmpty()); // basic mapping
    }

    @Test
    void getMe_throwsUnauthorized_whenUserNotFound() {
        OrgUserDetails me = mock(OrgUserDetails.class);
        when(me.getUsername()).thenReturn("me@acme.com");

        when(userRepo.findByEmailWithAuth("me@acme.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getMe(me));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void updateMe_updatesDisplayName_whenProvided() {
        OrgUserDetails me = mock(OrgUserDetails.class);
        when(me.getUsername()).thenReturn("me@acme.com");

        OrganizationEntity org = new OrganizationEntity();
        setId(org, 10L);

        UserEntity u = new UserEntity();
        setId(u, 2L);
        u.setEmail("me@acme.com");
        u.setDisplayName("Old");
        u.setPasswordHash("x");
        u.setOrganization(org);

        when(userRepo.findByEmailWithAuth("me@acme.com")).thenReturn(Optional.of(u));

        UserUpdateRequest req = new UserUpdateRequest();
        req.setDisplayName("New Name");

        UserResponse res = service.updateMe(me, req);

        assertEquals("New Name", res.getDisplayName());
        assertEquals("New Name", u.getDisplayName());
    }

    @Test
    void deleteUserInMyOrg_forbidden_whenTargetInDifferentOrg() {
        Long adminOrg = 10L;

        OrgUserDetails admin = mock(OrgUserDetails.class);
        when(admin.getOrganizationId()).thenReturn(adminOrg); // needed

        OrganizationEntity otherOrg = new OrganizationEntity();
        setId(otherOrg, 99L);

        UserEntity target = new UserEntity();
        setId(target, 50L);
        target.setOrganization(otherOrg);

        when(userRepo.findByIdWithAuth(50L)).thenReturn(Optional.of(target)); // needed

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteUserInMyOrg(admin, 50L));

        assertEquals(403, ex.getStatusCode().value());
        verify(userRepo, never()).delete(any());
    }

}
