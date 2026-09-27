package com.corebackend.auth.service;

import com.corebackend.auth.entity.AuthUser;

public interface TokenService {

    String generateToken(AuthUser user);

    Long getUserId(String token);

    String getUsername(String token);

    boolean validateToken(String token);
}