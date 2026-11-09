package com.corebackend.approval.service.impl;

import com.corebackend.approval.entity.ApprovalFlow;
import com.corebackend.approval.repository.ApprovalFlowRepository;
import com.corebackend.approval.service.ApprovalFlowService;
import com.corebackend.common.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.corebackend.approval.entity.ApprovalContext;
import com.corebackend.approval.service.ApprovalConditionEvaluator;
import java.util.List;
import java.util.Optional;

@Service
public class ApprovalFlowServiceImpl
        extends BaseServiceImpl<ApprovalFlow>
        implements ApprovalFlowService {

    private final ApprovalConditionEvaluator conditionEvaluator;
    private final ApprovalFlowRepository approvalFlowRepository;

    public ApprovalFlowServiceImpl(
            ApprovalFlowRepository approvalFlowRepository,
            ApprovalConditionEvaluator conditionEvaluator) {
        super(approvalFlowRepository);
        this.approvalFlowRepository = approvalFlowRepository;
        this.conditionEvaluator = conditionEvaluator;
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<ApprovalFlow> getByFlowCode(String flowCode) {
        if (flowCode == null || flowCode.isBlank()) {
            throw new IllegalArgumentException("Flow code is required");
        }

        return approvalFlowRepository
                .findByFlowCodeAndActive(flowCode.trim(), "Y")
                .filter(flow -> !flow.isDeleted());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalFlow> getMatchingFlows(
            String objectType,
            String operation) {

        if (objectType == null || objectType.isBlank()) {
            throw new IllegalArgumentException("Object type is required");
        }

        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("Operation is required");
        }

        return approvalFlowRepository
                .findByObjectTypeAndOperationAndActiveOrderByPriorityAsc(
                        objectType.trim(),
                        operation.trim(),
                        "Y"
                )
                .stream()
                .filter(flow -> !flow.isDeleted())
                .toList();
    }

    @Override
    protected void preCreate(ApprovalFlow flow) {
        validateFlow(flow);

        if (approvalFlowRepository.existsByFlowCode(
                flow.getFlowCode().trim())) {
            throw new IllegalArgumentException(
                    "Flow code already exists: " + flow.getFlowCode());
        }

        normalizeFlow(flow);
    }

    @Override
    protected void preUpdate(Long id, ApprovalFlow flow) {
        validateFlow(flow);

        if (approvalFlowRepository.existsByFlowCodeAndIdNot(
                flow.getFlowCode().trim(), id)) {
            throw new IllegalArgumentException(
                    "Flow code already exists: " + flow.getFlowCode());
        }

        normalizeFlow(flow);
    }

    private void validateFlow(ApprovalFlow flow) {
        if (flow == null) {
            throw new IllegalArgumentException("Approval flow is required");
        }

        requireValue(flow.getFlowCode(), "Flow code");
        requireValue(flow.getFlowName(), "Flow name");
        requireValue(flow.getObjectType(), "Object type");
        requireValue(flow.getOperation(), "Operation");
        requireValue(flow.getConditionType(), "Condition type");

        String conditionType = flow.getConditionType().trim();

        if (!conditionType.equals("ALWAYS")
                && !conditionType.equals("STATUS_TRANSITION")) {
            throw new IllegalArgumentException(
                    "Unsupported condition type: " + conditionType);
        }

        conditionEvaluator.validateConditionConfig(flow);

        if (flow.getPriority() == null || flow.getPriority() < 0) {
            throw new IllegalArgumentException(
                    "Priority must be zero or greater");
        }

        if (flow.getActive() == null
                || (!flow.getActive().equals("Y")
                && !flow.getActive().equals("N"))) {
            throw new IllegalArgumentException(
                    "Active must be Y or N");
        }
    }

    private void normalizeFlow(ApprovalFlow flow) {
        flow.setFlowCode(flow.getFlowCode().trim());
        flow.setFlowName(flow.getFlowName().trim());
        flow.setObjectType(flow.getObjectType().trim());
        flow.setOperation(flow.getOperation().trim());
        flow.setConditionType(flow.getConditionType().trim().toUpperCase());
    }

    private void requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalFlow> findMatchingFlows(ApprovalContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Approval context is required");
        }

        if (context.getObjectType() == null
                || context.getObjectType().isBlank()) {
            throw new IllegalArgumentException("Object type is required");
        }

        if (context.getOperation() == null
                || context.getOperation().isBlank()) {
            throw new IllegalArgumentException("Operation is required");
        }

        return getMatchingFlows(
                context.getObjectType(),
                context.getOperation()
        ).stream()
                .filter(flow -> conditionEvaluator.matches(flow, context))
                .toList();
    }

}