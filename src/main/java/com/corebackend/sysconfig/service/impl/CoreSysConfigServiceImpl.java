package com.corebackend.sysconfig.service.impl;

import com.corebackend.sysconfig.entity.CoreSysConfig;
import com.corebackend.sysconfig.repository.CoreSysConfigRepository;
import com.corebackend.sysconfig.service.CoreSysConfigService;
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