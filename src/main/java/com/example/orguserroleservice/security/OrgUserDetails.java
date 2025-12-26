package com.example.orguserroleservice.security;

import com.example.orguserroleservice.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class OrgUserDetails implements UserDetails {

    private final Long userId;
    private final String email;
    private final String passwordHash;
    private final Long organizationId; // can be null
    private final Set<String> roleNames;
    private final String displayName;

    public OrgUserDetails(UserEntity user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.organizationId = user.getOrganization().getId();

        this.roleNames = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
        this.displayName = user.getDisplayName();
    }

    public Long getUserId() { return userId; }
    public Long getOrganizationId() { return organizationId; }
    public Set<String> getRoleNames() { return roleNames; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleNames.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
    }
    public String getDisplayName() { return displayName; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
