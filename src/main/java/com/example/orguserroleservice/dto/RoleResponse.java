package com.example.orguserroleservice.dto;

import java.time.Instant;

public class RoleResponse {

    private Long id;
    private String name;
    private Long organizationId;
    private Instant createdAt;

    public RoleResponse(Long id, String name, Long organizationId, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.organizationId = organizationId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getOrganizationId() { return organizationId; }
    public Instant getCreatedAt() { return createdAt; }
}
