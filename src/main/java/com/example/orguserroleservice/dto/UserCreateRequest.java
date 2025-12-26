package com.example.orguserroleservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserCreateRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String displayName;

    @NotBlank
    private String password;

    @NotNull
    private Long organizationId; // or remove this if org is decided by your business rules

    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPassword() { return password; }
    public Long getOrganizationId() { return organizationId; }

    public void setEmail(String email) { this.email = email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setPassword(String password) { this.password = password; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}
