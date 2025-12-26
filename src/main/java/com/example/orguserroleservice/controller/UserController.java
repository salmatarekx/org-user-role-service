package com.example.orguserroleservice.controller;

import com.example.orguserroleservice.dto.*;
import com.example.orguserroleservice.security.OrgUserDetails;
import com.example.orguserroleservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    // Admin create user (NOT public)
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PostMapping("/create")
    public UserResponse createUser(
            @AuthenticationPrincipal OrgUserDetails admin,
            @Valid @RequestBody UserCreateRequest req
    ) {
        return users.createUserByAdmin(admin, req);
    }


    // Admin list users
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @GetMapping("/list")
    public Page<UserResponse> listUsers(
            @AuthenticationPrincipal OrgUserDetails me,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return users.listUsersForOrg(me, pageable);
    }

    // Get current user's profile
    @GetMapping("/get-profile")
    public UserResponse getProfile(@AuthenticationPrincipal OrgUserDetails me) {
        return users.getMe(me);
    }

    // Update current user's profile
    @PatchMapping("/update-profile")
    public UserResponse updateProfile(
            @AuthenticationPrincipal OrgUserDetails me,
            @Valid @RequestBody UserUpdateRequest req
    ) {
        return users.updateMe(me, req);
    }

    // Admin delete user
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public void deleteUser(
            @AuthenticationPrincipal OrgUserDetails admin,
            @PathVariable Long id
    ) {
        users.deleteUserInMyOrg(admin, id);
    }

    // Admin assign role
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PostMapping("/assign-role/{id}/{roleName}")
    public UserResponse assignRole(
            @AuthenticationPrincipal OrgUserDetails admin,
            @PathVariable Long id,
            @PathVariable String roleName
    ) {
        return users.assignRole(admin, id, roleName);
    }

    // Admin revoke role
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @DeleteMapping("/revoke-role/{id}/{roleName}")
    public UserResponse revokeRole(
            @AuthenticationPrincipal OrgUserDetails admin,
            @PathVariable Long id,
            @PathVariable String roleName
    ) {
        return users.revokeRole(admin, id, roleName);
    }
}
