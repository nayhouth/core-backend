package com.corebackend.common.service;

import com.corebackend.common.entity.BaseEntity;

import java.util.List;
import java.util.Optional;

public interface BaseService<T extends BaseEntity> {

    T create(T entity);

    Optional<T> getById(Long id);

    List<T> getAll();

    T update(Long id, T entity);

    void delete(Long id);
}