package com.corebackend.service.impl;

import com.corebackend.entity.BaseEntity;
import com.corebackend.repository.BaseRepository;
import com.corebackend.service.BaseService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<T extends BaseEntity>
        implements BaseService<T> {

    protected final BaseRepository<T> repository;

    protected BaseServiceImpl(BaseRepository<T> repository) {
        this.repository = repository;
    }

    @Override
    public T create(T entity) {
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setModNo(1L);
        entity.setDeleted(false);
        entity.setDeletedAt(null);

        return repository.save(entity);
    }

    @Override
    public Optional<T> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<T> getAll() {
        return repository.findAll();
    }

    @Override
    public T update(T entity) {
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setModNo(entity.getModNo() + 1);

        return repository.save(entity);
    }

    @Override
    public void delete(Long id) {
        Optional<T> optional = repository.findById(id);

        if (optional.isEmpty()) {
            return;
        }

        T entity = optional.get();

        entity.setDeleted(true);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setModNo(entity.getModNo() + 1);

        repository.save(entity);
    }
}