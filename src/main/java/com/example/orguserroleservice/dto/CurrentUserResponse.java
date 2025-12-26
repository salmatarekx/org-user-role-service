
package com.example.orguserroleservice.dto;
import java.util.Set;

public class CurrentUserResponse
{ private Long id; private String email; private String displayName; private Set<String> roles; public CurrentUserResponse(Long id, String email, String displayName, Set<String> roles) {this.id = id; this.email = email; this.displayName = displayName; this.roles = roles; } public Long getId() { return id; } public String getEmail() { return email; } public String getDisplayName() { return displayName; } public Set<String> getRoles() { return roles; } }