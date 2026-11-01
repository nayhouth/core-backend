package com.corebackend.approval.service.impl;

import com.corebackend.approval.entity.ApprovalRequestStep;
import com.corebackend.approval.repository.ApprovalRequestStepRepository;
import com.corebackend.approval.service.ApprovalRequestStepService;
import com.corebackend.common.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApprovalRequestStepServiceImpl
        extends BaseServiceImpl<ApprovalRequestStep>
        implements ApprovalRequestStepService {

    private final ApprovalRequestStepRepository approvalRequestStepRepository;

    public ApprovalRequestStepServiceImpl(
            ApprovalRequestStepRepository approvalRequestStepRepository
    ) {
        super(approvalRequestStepRepository);
        this.approvalRequestStepRepository = approvalRequestStepRepository;
    }

    @Override
    public List<ApprovalRequestStep> getByRequestId(Long requestId) {
        validateRequestId(requestId);

        return approvalRequestStepRepository
                .findByRequestIdAndIsDeletedFalseOrderByStepOrderAsc(requestId);
    }

    @Override
    public Optional<ApprovalRequestStep> getByRequestIdAndStepOrder(
            Long requestId,
            Integer stepOrder
    ) {
        validateRequestId(requestId);

        if (stepOrder == null || stepOrder <= 0) {
            throw new IllegalArgumentException("Valid step order is required");
        }

        return approvalRequestStepRepository
                .findByRequestIdAndStepOrderAndIsDeletedFalse(
                        requestId,
                        stepOrder
                );
    }

    private void validateRequestId(Long requestId) {
        if (requestId == null || requestId <= 0) {
            throw new IllegalArgumentException("Valid request ID is required");
        }
    }
}