package com.corebackend.approval.repository;

import com.corebackend.approval.entity.ApprovalRequestStep;
import com.corebackend.common.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalRequestStepRepository
        extends BaseRepository<ApprovalRequestStep> {

    List<ApprovalRequestStep>
    findByRequestIdAndIsDeletedFalseOrderByStepOrderAsc(Long requestId);

    Optional<ApprovalRequestStep>
    findByRequestIdAndStepOrderAndIsDeletedFalse(
            Long requestId,
            Integer stepOrder
    );

    boolean existsByRequestIdAndStepOrderAndIsDeletedFalse(
            Long requestId,
            Integer stepOrder
    );
}