package com.example.orguserroleservice.dto;

public class AuthTokenResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    public AuthTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
}
