package com.corebackend.approval.service.impl;

import com.corebackend.approval.entity.ApprovalRequest;
import com.corebackend.approval.repository.ApprovalRequestRepository;
import com.corebackend.approval.service.ApprovalFlowStepService;
import com.corebackend.approval.service.ApprovalRequestService;
import com.corebackend.approval.service.ApprovalRequestStepService;
import com.corebackend.common.service.impl.BaseServiceImpl;
import org.springframework.stereotype.Service;
import com.corebackend.approval.dto.ApprovalRequestSubmissionDto;
import com.corebackend.approval.dto.ApprovalRequestSubmissionResultDto;
import com.corebackend.approval.entity.ApprovalFlowStep;
import com.corebackend.approval.entity.ApprovalRequestStep;
import com.corebackend.approval.entity.ApprovalContext;
import com.corebackend.approval.entity.ApprovalFlow;
import com.corebackend.approval.service.ApprovalFlowService;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
public class ApprovalRequestServiceImpl
        extends BaseServiceImpl<ApprovalRequest>
        implements ApprovalRequestService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalFlowService approvalFlowService;
    private final ApprovalFlowStepService approvalFlowStepService;
    private final ApprovalRequestStepService approvalRequestStepService;

    public ApprovalRequestServiceImpl(
            ApprovalRequestRepository approvalRequestRepository,
            ApprovalFlowService approvalFlowService,
            ApprovalFlowStepService approvalFlowStepService,
            ApprovalRequestStepService approvalRequestStepService
    ) {
        super(approvalRequestRepository);
        this.approvalRequestRepository = approvalRequestRepository;
        this.approvalFlowService = approvalFlowService;
        this.approvalFlowStepService = approvalFlowStepService;
        this.approvalRequestStepService = approvalRequestStepService;
    }

    @Override
    public Optional<ApprovalRequest> getByRequestCode(String requestCode) {
        if (requestCode == null || requestCode.isBlank()) {
            throw new IllegalArgumentException("Request code is required");
        }

        return approvalRequestRepository.findByRequestCode(requestCode.trim())
                .filter(request -> !request.isDeleted());
    }

    @Override
    public List<ApprovalRequest> getByRequesterAndStatus(
            String requesterId,
            String status
    ) {
        if (requesterId == null || requesterId.isBlank()) {
            throw new IllegalArgumentException("Requester ID is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        return approvalRequestRepository
                .findByRequesterIdAndStatusOrderBySubmittedAtDesc(
                        requesterId.trim(),
                        status.trim()
                )
                .stream()
                .filter(request -> !request.isDeleted())
                .toList();
    }

    @Override
    public List<ApprovalRequest> getByObjectAndOperationAndStatus(
            String objectType,
            String objectId,
            String operation,
            String status
    ) {
        if (objectType == null || objectType.isBlank()) {
            throw new IllegalArgumentException("Object type is required");
        }
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("Object ID is required");
        }
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("Operation is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        return approvalRequestRepository
                .findByObjectTypeAndObjectIdAndOperationAndStatus(
                        objectType.trim(),
                        objectId.trim(),
                        operation.trim(),
                        status.trim()
                )
                .stream()
                .filter(request -> !request.isDeleted())
                .toList();
    }

    @Override
    @Transactional
    public ApprovalRequestSubmissionResultDto submit( ApprovalRequestSubmissionDto submission ) {

        validateSubmission(submission);

        ApprovalContext context = new ApprovalContext();
        context.setObjectType(submission.getObjectType().trim());
        context.setOperation(submission.getOperation().trim());
        context.setFromStatus(submission.getFromStatus());
        context.setToStatus(submission.getToStatus());

        List<ApprovalFlow> matchingFlows =
                approvalFlowService.findMatchingFlows(context);

        ApprovalRequestSubmissionResultDto result =
                new ApprovalRequestSubmissionResultDto();

        if (matchingFlows.isEmpty()) {
            result.setApprovalRequired(false);
            result.setApprovalRequest(null);
            result.setMessage("No approval required");
            return result;
        }

        List<ApprovalRequest> pendingRequests =
                approvalRequestRepository
                        .findByObjectTypeAndObjectIdAndOperationAndStatus(
                                submission.getObjectType().trim(),
                                submission.getObjectId().trim(),
                                submission.getOperation().trim(),
                                "PENDING"
                        );

        boolean hasPendingRequest = pendingRequests.stream()
                .anyMatch(request -> !request.isDeleted());

        if (hasPendingRequest) {
            throw new IllegalStateException(
                    "A pending approval request already exists for this object and operation"
            );
        }

        List<ApprovalFlowStep> flowSteps =
                approvalFlowStepService.getByFlowId(
                        matchingFlows.get(0).getId()
                );

        if (flowSteps.isEmpty()) {
            throw new IllegalStateException(
                    "The matched approval flow has no configured steps"
            );
        }

        ApprovalRequest request = new ApprovalRequest();
        request.setFlow(matchingFlows.get(0));
        request.setRequestCode("APR-" + UUID.randomUUID());
        request.setObjectType(submission.getObjectType().trim());
        request.setObjectId(submission.getObjectId().trim());
        request.setOperation(submission.getOperation().trim());
        request.setRequesterId(submission.getRequesterId().trim());
        request.setRequestData(submission.getRequestData());
        request.setStatus("PENDING");
        request.setSubmittedAt(LocalDateTime.now());

        ApprovalRequest createdRequest = create(request);

        for (ApprovalFlowStep flowStep : flowSteps) {
            ApprovalRequestStep requestStep = new ApprovalRequestStep();
            requestStep.setRequest(createdRequest);
            requestStep.setFlowStep(flowStep);
            requestStep.setStepOrder(flowStep.getStepOrder());
            requestStep.setStatus("PENDING");

            approvalRequestStepService.create(requestStep);
        }

        result.setApprovalRequired(true);
        result.setApprovalRequest(createdRequest);
        result.setMessage("Approval request submitted successfully");

        return result;
    }

    private void validateSubmission(ApprovalRequestSubmissionDto submission) {
        if (submission == null) {
            throw new IllegalArgumentException("Submission is required");
        }
        if (submission.getObjectType() == null
                || submission.getObjectType().isBlank()) {
            throw new IllegalArgumentException("Object type is required");
        }
        if (submission.getOperation() == null
                || submission.getOperation().isBlank()) {
            throw new IllegalArgumentException("Operation is required");
        }
        if (submission.getObjectId() == null
                || submission.getObjectId().isBlank()) {
            throw new IllegalArgumentException("Object ID is required");
        }
        if (submission.getRequesterId() == null
                || submission.getRequesterId().isBlank()) {
            throw new IllegalArgumentException("Requester ID is required");
        }
    }

    @Override
    @Transactional
    public ApprovalRequest approve(
            String requestCode,
            String approverId,
            String comment
    ) {
        return processDecision(requestCode, approverId, comment, true);
    }

    @Override
    @Transactional
    public ApprovalRequest reject(
            String requestCode,
            String approverId,
            String comment
    ) {
        return processDecision(requestCode, approverId, comment, false);
    }

    private ApprovalRequest processDecision(
            String requestCode,
            String approverId,
            String comment,
            boolean approved
    ) {
        if (requestCode == null || requestCode.isBlank()) {
            throw new IllegalArgumentException("Request code is required");
        }

        if (approverId == null || approverId.isBlank()) {
            throw new IllegalArgumentException("Approver ID is required");
        }

        ApprovalRequest request = getByRequestCode(requestCode.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException("Approval request not found")
                );

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException(
                    "Only pending approval requests can be acted on"
            );
        }

        List<ApprovalRequestStep> steps =
                approvalRequestStepService.getByRequestId(request.getId());

        ApprovalRequestStep currentStep = steps.stream()
                .filter(step -> "PENDING".equalsIgnoreCase(step.getStatus()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No pending approval step found")
                );

        ApprovalFlowStep flowStep = currentStep.getFlowStep();

        if (!"USER".equalsIgnoreCase(flowStep.getApproverType())) {
            throw new IllegalStateException(
                    "Unsupported approver type: " + flowStep.getApproverType()
            );
        }

        if (!approverId.trim().equalsIgnoreCase(flowStep.getApproverValue())) {
            throw new IllegalStateException(
                    "You are not authorized to act on this approval step"
            );
        }

        currentStep.setApproverId(approverId.trim());
        currentStep.setActionAt(LocalDateTime.now());
        currentStep.setComment(comment);
        currentStep.setStatus(approved ? "APPROVED" : "REJECTED");

        approvalRequestStepService.update(currentStep.getId(), currentStep);

        if (!approved) {
            request.setStatus("REJECTED");
        } else {
            boolean allApproved = steps.stream()
                    .allMatch(step ->
                            "APPROVED".equalsIgnoreCase(step.getStatus())
                    );

            if (allApproved) {
                request.setStatus("APPROVED");
            }
        }

        return update(request.getId(), request);
    }


}