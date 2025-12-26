package com.example.orguserroleservice.controller;

import com.example.orguserroleservice.dto.OrganizationResponse;
import com.example.orguserroleservice.security.OrgUserDetails;
import com.example.orguserroleservice.service.OrganizationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService orgService;

    public OrganizationController(OrganizationService orgService) {
        this.orgService = orgService;
    }

    @GetMapping("/get-current")
    public OrganizationResponse getCurrentOrganization(
            @AuthenticationPrincipal OrgUserDetails me
    ) {
        return orgService.getMyOrg(me);
    }
}
