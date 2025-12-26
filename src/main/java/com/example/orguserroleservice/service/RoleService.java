package com.example.orguserroleservice.service;

import com.example.orguserroleservice.dto.RoleCreateRequest;
import com.example.orguserroleservice.dto.RoleResponse;
import com.example.orguserroleservice.entity.OrganizationEntity;
import com.example.orguserroleservice.entity.RoleEntity;
import com.example.orguserroleservice.repository.OrganizationRepository;
import com.example.orguserroleservice.repository.RoleRepository;
import com.example.orguserroleservice.repository.UserRepository;
import com.example.orguserroleservice.security.OrgUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class RoleService {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final OrganizationRepository orgRepo;

    public RoleService(RoleRepository roleRepo,
                       UserRepository userRepo,
                       OrganizationRepository orgRepo) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.orgRepo = orgRepo;
    }

    @Transactional
    public RoleResponse createRole(OrgUserDetails admin, RoleCreateRequest req) {

        Long orgId = admin.getOrganizationId();
        if (orgId == null) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: you are not linked to an organization."
            );
        }

        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Invalid role name: role name cannot be empty."
            );
        }

        if (roleRepo.existsByOrganization_IdAndName(orgId, name)) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Cannot create role: role already exists in this organization."
            );
        }

        OrganizationEntity org = orgRepo.findById(orgId)
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Invalid organization."
                ));

        RoleEntity role = new RoleEntity();
        role.setName(name);
        role.setOrganization(org);

        RoleEntity saved = roleRepo.save(role);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles(OrgUserDetails me) {

        if (me.getOrganizationId() == null) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: you are not linked to an organization."
            );
        }

        return roleRepo.findAllByOrganization_Id(me.getOrganizationId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deleteRole(OrgUserDetails admin, String roleName) {

        Long orgId = admin.getOrganizationId();
        if (orgId == null) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: you are not linked to an organization."
            );
        }

        String trimmed = roleName == null ? "" : roleName.trim();
        if (trimmed.isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Invalid role name."
            );
        }

        RoleEntity role = roleRepo.findByOrganization_IdAndName(orgId, trimmed)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Role not found in this organization."
                ));

        if (userRepo.existsAnyUserWithRoleId(role.getId())) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Cannot delete role: role is assigned to one or more users."
            );
        }

        roleRepo.delete(role);
        // Success → controller returns 204 No Content
    }

    private RoleResponse toDto(RoleEntity r) {
        return new RoleResponse(
                r.getId(),
                r.getName(),
                r.getOrganization().getId(),
                r.getCreatedAt()
        );
    }
}
