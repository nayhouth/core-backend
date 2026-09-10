package com.corebackend.service.impl;

import com.corebackend.entity.CoreSysConfig;
import com.corebackend.repository.CoreSysConfigRepository;
import com.corebackend.service.CoreSysConfigService;
import org.springframework.stereotype.Service;

@Service
public class CoreSysConfigServiceImpl
        implements CoreSysConfigService {

    private final CoreSysConfigRepository coreSysConfigRepository;

    public CoreSysConfigServiceImpl(
            CoreSysConfigRepository coreSysConfigRepository) {
        this.coreSysConfigRepository =
                coreSysConfigRepository;
    }

    @Override
    public String getConfigValue(
            String configGroup,
            String configKey) {

        return coreSysConfigRepository
                .findByConfigGroupAndConfigKeyAndActive(
                        configGroup,
                        configKey,
                        "Y"
                )
                .filter(config -> !config.isDeleted())
                .map(CoreSysConfig::getConfigValue)
                .orElse(null);
    }
}