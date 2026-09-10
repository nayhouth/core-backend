package com.corebackend.repository;

import com.corebackend.entity.CoreSysConfig;

import java.util.Optional;

public interface CoreSysConfigRepository
        extends BaseRepository<CoreSysConfig> {

    Optional<CoreSysConfig> findByConfigGroupAndConfigKeyAndActive(
            String configGroup,
            String configKey,
            String active
    );
}