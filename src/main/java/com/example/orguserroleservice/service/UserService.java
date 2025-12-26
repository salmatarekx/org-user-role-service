package com.example.orguserroleservice.service;

import com.example.orguserroleservice.dto.*;
import com.example.orguserroleservice.entity.*;
import com.example.orguserroleservice.repository.*;
import com.example.orguserroleservice.security.OrgUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final OrganizationRepository orgRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo,
                       OrganizationRepository orgRepo,
                       RoleRepository roleRepo,
                       PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.orgRepo = orgRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUserByAdmin(OrgUserDetails admin, UserCreateRequest req) {

        if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Email already exists."
            );
        }

        if (admin.getOrganizationId() == null) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: you are not linked to an organization."
            );
        }

        if (!admin.getOrganizationId().equals(req.getOrganizationId())) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: you can only create users in your organization."
            );
        }

        OrganizationEntity org = orgRepo.findById(admin.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Invalid organization."
                ));

        RoleEntity defaultRole = roleRepo
                .findByOrganization_IdAndName(org.getId(), "ORG_USER")
                .orElseThrow(() -> new ResponseStatusException(
                        BAD_REQUEST,
                        "Default role ORG_USER is not configured."
                ));

        UserEntity u = new UserEntity();
        u.setEmail(req.getEmail());
        u.setDisplayName(req.getDisplayName());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setOrganization(org);
        u.getRoles().add(defaultRole);

        UserEntity saved = userRepo.save(u);
        return toUserResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsersForOrg(OrgUserDetails me, Pageable pageable) {
        if (me.getOrganizationId() == null) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied: you are not linked to an organization.");
        }

        return userRepo.findAllByOrganizationId(me.getOrganizationId(), pageable)
                .map(this::toUserResponse); // ✅ includes roles
    }


    @Transactional(readOnly = true)
    public UserResponse getMe(OrgUserDetails me) {

        UserEntity u = userRepo.findByEmailWithAuth(me.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        UNAUTHORIZED,
                        "User not found."
                ));

        return toUserResponse(u);
    }

    @Transactional
    public UserResponse updateMe(OrgUserDetails me, UserUpdateRequest req) {

        UserEntity u = userRepo.findByEmailWithAuth(me.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        UNAUTHORIZED,
                        "User not found."
                ));

        if (req.getDisplayName() != null && !req.getDisplayName().isBlank()) {
            u.setDisplayName(req.getDisplayName());
        }

        return toUserResponse(u);
    }

    @Transactional
    public void deleteUserInMyOrg(OrgUserDetails admin, Long userId) {

        UserEntity target = userRepo.findByIdWithAuth(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "User not found."
                ));

        if (admin.getOrganizationId() == null) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: you are not linked to an organization."
            );
        }

        if (!target.getOrganization().getId().equals(admin.getOrganizationId())) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: cannot delete user from another organization."
            );
        }

        if (target.getId().equals(admin.getUserId())) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "You cannot delete your own account."
            );
        }

        userRepo.delete(target);
    }

    @Transactional
    public UserResponse assignRole(OrgUserDetails admin, Long userId, String roleName) {

        UserEntity target = userRepo.findByIdWithAuth(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "User not found."
                ));

        if (!admin.getOrganizationId().equals(target.getOrganization().getId())) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: different organization."
            );
        }

        RoleEntity role = roleRepo.findByOrganization_IdAndName(
                        admin.getOrganizationId(), roleName)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Role not found."
                ));

        target.getRoles().add(role);
        return toUserResponse(target);
    }

    @Transactional
    public UserResponse revokeRole(OrgUserDetails admin, Long userId, String roleName) {

        UserEntity target = userRepo.findByIdWithAuth(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "User not found."
                ));

        if (!admin.getOrganizationId().equals(target.getOrganization().getId())) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: different organization."
            );
        }

        RoleEntity role = roleRepo.findByOrganization_IdAndName(
                        admin.getOrganizationId(), roleName)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Role not found."
                ));

        target.getRoles().removeIf(r -> r.getId().equals(role.getId()));
        return toUserResponse(target);
    }

    private UserResponse toUserResponse(UserEntity u) {
        Set<String> roles = u.getRoles()
                .stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getDisplayName(),
                u.getOrganization().getId(),
                roles,
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }

    private UserResponse toUserResponseBasic(UserEntity u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getDisplayName(),
                u.getOrganization().getId(),
                Set.of(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
