package com.corebackend.sysconfig.repository;

import com.corebackend.common.repository.BaseRepository;
import com.corebackend.sysconfig.entity.CoreSysConfig;

import java.util.Optional;

public interface CoreSysConfigRepository
        extends BaseRepository<CoreSysConfig> {

    Optional<CoreSysConfig> findByConfigGroupAndConfigKeyAndActive(
            String configGroup,
            String configKey,
            String active
    );
}