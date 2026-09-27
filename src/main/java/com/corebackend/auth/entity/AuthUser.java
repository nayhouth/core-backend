package com.corebackend.auth.entity;

public interface AuthUser {

    Long getId();

    String getUsername();

    String getPassword();

    boolean isActive();
}