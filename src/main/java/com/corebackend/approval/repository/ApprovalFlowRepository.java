package com.corebackend.approval.repository;

import com.corebackend.approval.entity.ApprovalFlow;
import com.corebackend.common.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalFlowRepository
        extends BaseRepository<ApprovalFlow> {

    Optional<ApprovalFlow> findByFlowCodeAndActive(
            String flowCode,
            String active
    );

    List<ApprovalFlow> findByObjectTypeAndOperationAndActiveOrderByPriorityAsc(
            String objectType,
            String operation,
            String active
    );

    boolean existsByFlowCode(String flowCode);

    boolean existsByFlowCodeAndIdNot(String flowCode, Long id);
}