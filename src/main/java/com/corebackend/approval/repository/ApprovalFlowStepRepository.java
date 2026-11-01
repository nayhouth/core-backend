package com.corebackend.approval.repository;

import com.corebackend.approval.entity.ApprovalFlowStep;
import com.corebackend.common.repository.BaseRepository;

import java.util.List;

public interface ApprovalFlowStepRepository
        extends BaseRepository<ApprovalFlowStep> {

    List<ApprovalFlowStep> findByFlowIdAndIsDeletedFalseOrderByStepOrderAsc(
            Long flowId
    );

    boolean existsByFlowIdAndStepOrderAndIsDeletedFalse(
            Long flowId,
            Integer stepOrder
    );

    boolean existsByFlowIdAndStepOrderAndIsDeletedFalseAndIdNot(
            Long flowId,
            Integer stepOrder,
            Long id
    );

}