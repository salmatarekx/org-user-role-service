package com.example.orguserroleservice.service;

import com.example.orguserroleservice.dto.RoleCreateRequest;
import com.example.orguserroleservice.dto.RoleResponse;
import com.example.orguserroleservice.entity.OrganizationEntity;
import com.example.orguserroleservice.entity.RoleEntity;
import com.example.orguserroleservice.repository.OrganizationRepository;
import com.example.orguserroleservice.repository.RoleRepository;
import com.example.orguserroleservice.repository.UserRepository;
import com.example.orguserroleservice.security.OrgUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    RoleRepository roleRepo;
    @Mock
    UserRepository userRepo;
    @Mock
    OrganizationRepository orgRepo;

    RoleService service;

    @BeforeEach
    void setup() {
        service = new RoleService(roleRepo, userRepo, orgRepo);
    }

    private OrgUserDetails mockAdmin(Long orgId) {
        OrgUserDetails admin = mock(OrgUserDetails.class);
        when(admin.getOrganizationId()).thenReturn(orgId);
        return admin;
    }

    @Test
    void createRole_success() {
        Long orgId = 10L;
        OrgUserDetails admin = mockAdmin(orgId);

        RoleCreateRequest req = new RoleCreateRequest();
        req.setName("ORG_MANAGER");

        when(roleRepo.existsByOrganization_IdAndName(orgId, "ORG_MANAGER")).thenReturn(false);

        OrganizationEntity org = new OrganizationEntity();
        org.setName("Acme");
        // id getter needed in response mapping -> mock it via spy or set via reflection not required if not used directly
        // but your toDto reads org.getId(), so we must have an id.
        // simplest: spy + stub getId
        OrganizationEntity orgSpy = spy(org);
        doReturn(orgId).when(orgSpy).getId();

        when(orgRepo.findById(orgId)).thenReturn(Optional.of(orgSpy));

        RoleEntity saved = new RoleEntity();
        saved.setName("ORG_MANAGER");
        saved.setOrganization(orgSpy);

        RoleEntity savedSpy = spy(saved);
        doReturn(77L).when(savedSpy).getId();
        doReturn(Instant.parse("2025-01-01T00:00:00Z")).when(savedSpy).getCreatedAt();

        when(roleRepo.save(any(RoleEntity.class))).thenReturn(savedSpy);

        RoleResponse res = service.createRole(admin, req);

        assertEquals(77L, res.getId());
        assertEquals("ORG_MANAGER", res.getName());
        assertEquals(orgId, res.getOrganizationId());

        verify(roleRepo).existsByOrganization_IdAndName(orgId, "ORG_MANAGER");
        verify(orgRepo).findById(orgId);
        verify(roleRepo).save(any(RoleEntity.class));
    }

    @Test
    void createRole_conflict_whenExists() {
        Long orgId = 10L;
        OrgUserDetails admin = mockAdmin(orgId);

        RoleCreateRequest req = new RoleCreateRequest();
        req.setName("ORG_USER");

        when(roleRepo.existsByOrganization_IdAndName(orgId, "ORG_USER")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createRole(admin, req));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().toLowerCase().contains("already"));

        verify(roleRepo).existsByOrganization_IdAndName(orgId, "ORG_USER");
        verifyNoInteractions(orgRepo);
        verify(roleRepo, never()).save(any());
    }

    @Test
    void listRoles_returnsOnlyOrgRoles() {
        Long orgId = 10L;
        OrgUserDetails me = mockAdmin(orgId);

        OrganizationEntity org = new OrganizationEntity();
        OrganizationEntity orgSpy = spy(org);
        doReturn(orgId).when(orgSpy).getId();

        RoleEntity r1 = new RoleEntity();
        r1.setName("ORG_ADMIN");
        r1.setOrganization(orgSpy);

        RoleEntity r1Spy = spy(r1);
        doReturn(1L).when(r1Spy).getId();
        doReturn(Instant.parse("2025-01-01T00:00:00Z")).when(r1Spy).getCreatedAt();

        when(roleRepo.findAllByOrganization_Id(orgId)).thenReturn(List.of(r1Spy));

        List<RoleResponse> res = service.listRoles(me);

        assertEquals(1, res.size());
        assertEquals("ORG_ADMIN", res.get(0).getName());
        assertEquals(orgId, res.get(0).getOrganizationId());

        verify(roleRepo).findAllByOrganization_Id(orgId);
    }

    @Test
    void deleteRole_conflict_whenAssignedToUsers() {
        Long orgId = 10L;
        OrgUserDetails admin = mockAdmin(orgId);

        RoleEntity role = mock(RoleEntity.class);
        when(role.getId()).thenReturn(5L);

        when(roleRepo.findByOrganization_IdAndName(orgId, "ORG_USER"))
                .thenReturn(Optional.of(role));
        when(userRepo.existsAnyUserWithRoleId(5L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteRole(admin, "ORG_USER"));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().toLowerCase().contains("assigned"));

        verify(roleRepo).findByOrganization_IdAndName(orgId, "ORG_USER");
        verify(userRepo).existsAnyUserWithRoleId(5L);
        verify(roleRepo, never()).delete(any());
        verifyNoMoreInteractions(roleRepo, userRepo);
        verifyNoInteractions(orgRepo);
    }
    @Test
    void deleteRole_notFound_whenRoleMissing() {
        Long orgId = 10L;
        OrgUserDetails admin = mockAdmin(orgId);

        when(roleRepo.findByOrganization_IdAndName(orgId, "ORG_USER"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteRole(admin, "ORG_USER"));

        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().toLowerCase().contains("not found"));

        verify(roleRepo).findByOrganization_IdAndName(orgId, "ORG_USER");
        verifyNoInteractions(userRepo, orgRepo);
        verifyNoMoreInteractions(roleRepo);
    }

    @Test
    void deleteRole_success_deletesRole() {
        Long orgId = 10L;
        OrgUserDetails admin = mockAdmin(orgId);

        RoleEntity role = mock(RoleEntity.class);
        when(role.getId()).thenReturn(5L);

        when(roleRepo.findByOrganization_IdAndName(orgId, "ORG_USER"))
                .thenReturn(Optional.of(role));
        when(userRepo.existsAnyUserWithRoleId(5L)).thenReturn(false);

        assertDoesNotThrow(() -> service.deleteRole(admin, "ORG_USER"));

        verify(roleRepo).findByOrganization_IdAndName(orgId, "ORG_USER");
        verify(userRepo).existsAnyUserWithRoleId(5L);
        verify(roleRepo).delete(role);
        verifyNoInteractions(orgRepo);
        verifyNoMoreInteractions(roleRepo, userRepo);
    }

}
