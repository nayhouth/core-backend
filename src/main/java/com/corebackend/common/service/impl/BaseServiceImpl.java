package com.corebackend.common.service.impl;

import com.corebackend.common.entity.BaseEntity;
import com.corebackend.common.repository.BaseRepository;
import com.corebackend.common.service.BaseService;
import com.corebackend.common.util.EntityUpdateUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        preCreate(entity);

        LocalDateTime now = LocalDateTime.now();
        String username = getCurrentUsername();

        entity.setCreatedAt(now);
        entity.setUpdatedAt(null);
        entity.setModNo(1L);
        entity.setDeleted(false);
        entity.setDeletedAt(null);

        entity.setCreatedBy(username);
        entity.setUpdatedBy(null);
        entity.setDeletedBy(null);

        T result = repository.save(entity);

        postCreate(result);

        return result;
    }

    @Override
    public Optional<T> getById(Long id) {

        preGetById(id);

        Optional<T> result = repository.findById(id);

        postGetById(id, result);

        return result;
    }

    @Override
    public List<T> getAll() {

        preGetAll();

        List<T> result = repository.findAll();

        postGetAll(result);

        return result;
    }

    @Override
    public T update(Long id, T entity) {

        preUpdate(id, entity);

        Optional<T> optional = repository.findById(id);

        if (optional.isEmpty()) {
            return null;
        }

        T existing = optional.get();

        EntityUpdateUtil.copyFields(entity, existing);

        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(getCurrentUsername());
        existing.setModNo(existing.getModNo() + 1);

        T result = repository.save(existing);

        postUpdate(result);

        return result;
    }

    @Override
    public void delete(Long id) {

        preDelete(id);

        Optional<T> optional = repository.findById(id);

        if (optional.isEmpty()) {
            return;
        }

        T entity = optional.get();

        LocalDateTime now = LocalDateTime.now();
        String username = getCurrentUsername();

        entity.setDeleted(true);
        entity.setDeletedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedBy(username);
        entity.setUpdatedBy(username);
        entity.setModNo(entity.getModNo() + 1);

        repository.save(entity);

        postDelete(id);
    }

    private String getCurrentUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            return null;
        }

        return authentication.getName();
    }


    // =========================
    // PRE ACTIONS
    // =========================

    protected void preCreate(T entity) {
    }

    protected void preGetById(Long id) {
    }

    protected void preGetAll() {
    }

    protected void preUpdate(Long id, T entity) {
    }

    protected void preDelete(Long id) {
    }


    // =========================
    // POST ACTIONS
    // =========================

    protected void postCreate(T entity) {
    }

    protected void postGetById(Long id, Optional<T> result) {
    }

    protected void postGetAll(List<T> result) {
    }

    protected void postUpdate(T entity) {
    }

    protected void postDelete(Long id) {
    }
}