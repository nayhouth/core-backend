package com.corebackend.auth.controller;

import com.corebackend.auth.entity.LoginRequest;
import com.corebackend.auth.entity.LoginResponse;
import com.corebackend.auth.entity.AuthUser;
import com.corebackend.auth.service.AuthService;
import com.corebackend.auth.service.TokenService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(
            AuthService authService,
            TokenService tokenService) {

        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {

        AuthUser user = authService.authenticate(
                request.getUsername(),
                request.getPassword()
        );

        String token = tokenService.generateToken(user);

        return new LoginResponse(token);
    }
}