package com.corebackend.auth.service;

import com.corebackend.auth.entity.AuthUser;

public interface AuthService {

    AuthUser authenticate(String username, String password);
}