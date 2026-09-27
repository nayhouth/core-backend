package com.corebackend.auth.service;

import com.corebackend.auth.entity.AuthUser;

import java.util.Optional;

public interface AuthUserProvider {

    Optional<AuthUser> findByUsername(String username);
}