package com.corebackend.auth.service.impl;

import com.corebackend.auth.entity.AuthUser;
import com.corebackend.auth.exception.AuthenticationException;
import com.corebackend.auth.service.AuthService;
import com.corebackend.auth.service.AuthUserProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthUserProvider authUserProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            AuthUserProvider authUserProvider,
            PasswordEncoder passwordEncoder) {

        this.authUserProvider = authUserProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthUser authenticate(
            String username,
            String password) {

        AuthUser user = authUserProvider
                .findByUsername(username)
                .orElseThrow(() ->
                        new AuthenticationException(
                                "Invalid username or password"
                        ));

        if (!user.isActive()) {
            throw new AuthenticationException(
                    "User account is inactive"
            );
        }

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            throw new AuthenticationException(
                    "Invalid username or password"
            );
        }

        return user;
    }
}