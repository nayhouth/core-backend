package com.corebackend.service;

import com.corebackend.entity.BaseEntity;

import java.util.List;
import java.util.Optional;

public interface BaseService<T extends BaseEntity> {

    T create(T entity);

    Optional<T> getById(Long id);

    List<T> getAll();

    T update(T entity);

    void delete(Long id);
}