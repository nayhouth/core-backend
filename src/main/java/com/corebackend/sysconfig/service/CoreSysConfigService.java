package com.corebackend.sysconfig.service;

public interface CoreSysConfigService {

    String getConfigValue(
            String configGroup,
            String configKey
    );
}