package com.example.orguserroleservice.controller;

import com.example.orguserroleservice.dto.RoleCreateRequest;
import com.example.orguserroleservice.dto.RoleResponse;
import com.example.orguserroleservice.security.OrgUserDetails;
import com.example.orguserroleservice.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roles;

    public RoleController(RoleService roles) {
        this.roles = roles;
    }

    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PostMapping("/create")
    public RoleResponse createRole(
            @AuthenticationPrincipal OrgUserDetails admin,
            @Valid @RequestBody RoleCreateRequest req
    ) {
        return roles.createRole(admin, req);
    }
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @GetMapping("/list")
    public List<RoleResponse> listRoles(@AuthenticationPrincipal OrgUserDetails me) {
        return roles.listRoles(me);
    }

    @PreAuthorize("hasRole('ORG_ADMIN')")
    @DeleteMapping("/delete/{roleName}")
    public void deleteRole(
            @AuthenticationPrincipal OrgUserDetails admin,
            @PathVariable String roleName
    ) {
        roles.deleteRole(admin, roleName);
    }
}
