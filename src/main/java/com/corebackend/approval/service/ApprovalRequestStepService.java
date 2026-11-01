package com.corebackend.approval.service;

import com.corebackend.approval.entity.ApprovalRequestStep;
import com.corebackend.common.service.BaseService;

import java.util.List;
import java.util.Optional;

public interface ApprovalRequestStepService
        extends BaseService<ApprovalRequestStep> {

    List<ApprovalRequestStep> getByRequestId(Long requestId);

    Optional<ApprovalRequestStep> getByRequestIdAndStepOrder(
            Long requestId,
            Integer stepOrder
    );
}