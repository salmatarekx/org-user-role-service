package com.example.orguserroleservice.service;

import com.example.orguserroleservice.dto.OrganizationResponse;
import com.example.orguserroleservice.entity.OrganizationEntity;
import com.example.orguserroleservice.repository.OrganizationRepository;
import com.example.orguserroleservice.security.OrgUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrganizationService {

    private final OrganizationRepository orgRepo;

    public OrganizationService(OrganizationRepository orgRepo) {
        this.orgRepo = orgRepo;
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getMyOrg(OrgUserDetails me) {

        if (me.getOrganizationId() == null) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "Access denied: you are not linked to any organization."
            );
        }

        OrganizationEntity org = orgRepo.findById(me.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Organization not found."
                ));

        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getCreatedAt()
        );
    }
}
