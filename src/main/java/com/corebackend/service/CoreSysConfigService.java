package com.corebackend.service;

public interface CoreSysConfigService {

    String getConfigValue(
            String configGroup,
            String configKey
    );
}