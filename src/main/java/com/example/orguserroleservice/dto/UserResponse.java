package com.example.orguserroleservice.dto;

import java.time.Instant;
import java.util.Set;

public class UserResponse {
    private Long id;
    private String email;
    private String displayName;
    private Long organizationId;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse(Long id, String email, String displayName, Long organizationId,
                        Set<String> roles, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.organizationId = organizationId;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public Long getOrganizationId() { return organizationId; }
    public Set<String> getRoles() { return roles; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
