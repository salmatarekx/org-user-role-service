package com.example.orguserroleservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_roles_org_name", columnNames = {"organization_id", "name"})
)
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // e.g., ORG_ADMIN, ORG_USER

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public OrganizationEntity getOrganization() { return organization; }
    public Instant getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setOrganization(OrganizationEntity organization) { this.organization = organization; }
}
