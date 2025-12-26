package com.example.orguserroleservice.controller;

import com.example.orguserroleservice.dto.AuthLoginRequest;
import com.example.orguserroleservice.dto.AuthTokenResponse;
import com.example.orguserroleservice.dto.CurrentUserResponse;
import com.example.orguserroleservice.security.JwtService;
import com.example.orguserroleservice.security.OrgUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody AuthLoginRequest req) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        var principal = (OrgUserDetails) auth.getPrincipal();
        return new AuthTokenResponse(jwtService.generateAccessToken(principal));
    }

    @GetMapping("/get-current-user")
    public CurrentUserResponse getCurrentUser(
            @AuthenticationPrincipal OrgUserDetails user
    ) {
        return new CurrentUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRoleNames()
        );
    }

}




