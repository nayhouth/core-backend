package com.corebackend.approval.service.impl;

import com.corebackend.approval.entity.ApprovalFlowStep;
import com.corebackend.approval.repository.ApprovalFlowStepRepository;
import com.corebackend.approval.service.ApprovalFlowStepService;
import com.corebackend.common.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApprovalFlowStepServiceImpl
        extends BaseServiceImpl<ApprovalFlowStep>
        implements ApprovalFlowStepService {

    private final ApprovalFlowStepRepository approvalFlowStepRepository;

    public ApprovalFlowStepServiceImpl(
            ApprovalFlowStepRepository approvalFlowStepRepository
    ) {
        super(approvalFlowStepRepository);
        this.approvalFlowStepRepository = approvalFlowStepRepository;
    }

    @Override
    public List<ApprovalFlowStep> getByFlowId(Long flowId) {
        if (flowId == null || flowId <= 0) {
            throw new IllegalArgumentException("Valid flow ID is required");
        }

        return approvalFlowStepRepository
                .findByFlowIdAndIsDeletedFalseOrderByStepOrderAsc(flowId);
    }

    @Override
    protected void preCreate(ApprovalFlowStep entity) {
        validateStep(entity);

        boolean exists = approvalFlowStepRepository
                .existsByFlowIdAndStepOrderAndIsDeletedFalse(
                        entity.getFlow().getId(),
                        entity.getStepOrder()
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "Step order already exists for this approval flow"
            );
        }
    }

    @Override
    protected void preUpdate(Long id, ApprovalFlowStep entity) {
        validateStep(entity);

        boolean exists = approvalFlowStepRepository
                .existsByFlowIdAndStepOrderAndIsDeletedFalseAndIdNot(
                        entity.getFlow().getId(),
                        entity.getStepOrder(),
                        id
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "Step order already exists for this approval flow"
            );
        }
    }

    private void validateStep(ApprovalFlowStep entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Approval flow step is required");
        }

        if (entity.getFlow() == null || entity.getFlow().getId() == null) {
            throw new IllegalArgumentException("Approval flow is required");
        }

        if (entity.getStepOrder() == null || entity.getStepOrder() <= 0) {
            throw new IllegalArgumentException(
                    "Step order must be greater than zero");
        }

        if (entity.getApproverType() == null
                || entity.getApproverType().isBlank()) {
            throw new IllegalArgumentException("Approver type is required");
        }

        if (entity.getApproverValue() == null
                || entity.getApproverValue().isBlank()) {
            throw new IllegalArgumentException("Approver value is required");
        }

        entity.setApproverType(entity.getApproverType().trim().toUpperCase());
        entity.setApproverValue(entity.getApproverValue().trim());

        if (!"USER".equals(entity.getApproverType())) {
            throw new IllegalArgumentException(
                    "Unsupported approver type: " + entity.getApproverType());
        }

        if (entity.getStepName() != null) {
            entity.setStepName(entity.getStepName().trim());
        }
    }

}