package com.corebackend.approval.service.impl;

import com.corebackend.approval.dto.ApprovalRequestSubmissionDto;
import com.corebackend.approval.dto.ApprovalRequestSubmissionResultDto;
import com.corebackend.approval.entity.ApprovalFlow;
import com.corebackend.approval.entity.ApprovalRequest;
import com.corebackend.approval.repository.ApprovalRequestRepository;
import com.corebackend.approval.service.ApprovalFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.corebackend.approval.service.ApprovalFlowStepService;
import com.corebackend.approval.service.ApprovalRequestStepService;

import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.corebackend.approval.entity.ApprovalFlowStep;
import com.corebackend.approval.entity.ApprovalRequestStep;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ApprovalRequestServiceImplTest {

    @Mock
    private ApprovalFlowStepService approvalFlowStepService;

    @Mock
    private ApprovalRequestStepService approvalRequestStepService;
    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private ApprovalFlowService approvalFlowService;

    @InjectMocks
    private ApprovalRequestServiceImpl approvalRequestService;

    private ApprovalRequestSubmissionDto submission;

    @BeforeEach
    void setUp() {
        submission = new ApprovalRequestSubmissionDto();
        submission.setObjectType("TASK");
        submission.setOperation("UPDATE");
        submission.setFromStatus("DRAFT");
        submission.setToStatus("SUBMITTED");
        submission.setObjectId("TASK-001");
        submission.setRequesterId("user-001");
        submission.setRequestData("{\"title\":\"Updated task\"}");
    }

    @Test
    void submit_whenNoMatchingFlow_returnsNoApprovalRequired() {
        when(approvalFlowService.findMatchingFlows(any()))
                .thenReturn(List.of());

        ApprovalRequestSubmissionResultDto result =
                approvalRequestService.submit(submission);

        assertFalse(result.isApprovalRequired());
        assertNull(result.getApprovalRequest());
        assertEquals("No approval required", result.getMessage());

        verify(approvalRequestRepository, never()).save(any());
    }

    @Test
    void submit_whenPendingRequestExists_throwsException() {
        ApprovalFlow flow = new ApprovalFlow();
        when(approvalFlowService.findMatchingFlows(any()))
                .thenReturn(List.of(flow));

        ApprovalRequest existingRequest = new ApprovalRequest();
        existingRequest.setDeleted(false);

        when(approvalRequestRepository
                .findByObjectTypeAndObjectIdAndOperationAndStatus(
                        "TASK", "TASK-001", "UPDATE", "PENDING"))
                .thenReturn(List.of(existingRequest));

        assertThrows(
                IllegalStateException.class,
                () -> approvalRequestService.submit(submission)
        );

        verify(approvalRequestRepository, never()).save(any());
    }

    @Test
    void submit_whenSubmissionIsNull_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> approvalRequestService.submit(null)
        );
    }

    @Test
    void submit_whenObjectIdIsMissing_throwsException() {
        submission.setObjectId(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalRequestService.submit(submission)
        );
    }

    @Test
    void submit_whenMatchingFlowAndNoPendingRequest_createsPendingRequest() {
        ApprovalFlow flow = new ApprovalFlow();

        when(approvalFlowService.findMatchingFlows(any()))
                .thenReturn(List.of(flow));

        when(approvalRequestRepository
                .findByObjectTypeAndObjectIdAndOperationAndStatus(
                        "TASK", "TASK-001", "UPDATE", "PENDING"))
                .thenReturn(List.of());

        when(approvalRequestRepository.save(any(ApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(approvalFlowStepService.getByFlowId(any()))
                .thenReturn(List.of(createFlowStep(1)));

        ApprovalRequestSubmissionResultDto result =
                approvalRequestService.submit(submission);

        assertTrue(result.isApprovalRequired());
        assertNotNull(result.getApprovalRequest());
        assertEquals("PENDING", result.getApprovalRequest().getStatus());
        assertEquals("TASK-001", result.getApprovalRequest().getObjectId());
        assertEquals(flow, result.getApprovalRequest().getFlow());
        assertTrue(result.getApprovalRequest().getRequestCode().startsWith("APR-"));

        verify(approvalRequestRepository).save(any(ApprovalRequest.class));
    }

    @Test
    void submit_whenCreatingRequestStepFails_throwsException() {
        ApprovalFlow flow = mock(ApprovalFlow.class);
        when(flow.getId()).thenReturn(1L);

        when(approvalFlowService.findMatchingFlows(any()))
                .thenReturn(List.of(flow));

        when(approvalRequestRepository
                .findByObjectTypeAndObjectIdAndOperationAndStatus(
                        anyString(), anyString(), anyString(), eq("PENDING")))
                .thenReturn(List.of());

        when(approvalFlowStepService.getByFlowId(1L))
                .thenReturn(List.of(createFlowStep(1)));

        when(approvalRequestRepository.save(any(ApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(approvalRequestStepService.create(any(ApprovalRequestStep.class)))
                .thenThrow(new RuntimeException("Simulated step creation failure"));

        assertThrows(
                RuntimeException.class,
                () -> approvalRequestService.submit(submission)
        );
    }

    private ApprovalFlowStep createFlowStep(int stepOrder) {
        ApprovalFlowStep step = new ApprovalFlowStep();
        step.setFlow(mock(ApprovalFlow.class));
        step.setStepOrder(stepOrder);
        step.setApproverType("USER");
        step.setApproverValue("user-001");
        return step;
    }

    @Test
    void approve_whenMultipleStepsExist_keepsRequestPendingUntilAllStepsApproved() {
        // Arrange: pending approval request
        ApprovalRequest request = mock(ApprovalRequest.class);

        when(request.getId()).thenReturn(1L);
        when(request.getStatus()).thenReturn("PENDING");
        when(request.isDeleted()).thenReturn(false);
        when(request.getModNo()).thenReturn(0L);

        when(approvalRequestRepository.findByRequestCode("APR-001"))
                .thenReturn(Optional.of(request));

        when(approvalRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(approvalRequestRepository.save(any(ApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Arrange: first approval step
        ApprovalFlowStep flowStep = mock(ApprovalFlowStep.class);

        when(flowStep.getApproverType()).thenReturn("USER");
        when(flowStep.getApproverValue()).thenReturn("user-001");

        ApprovalRequestStep step1 = mock(ApprovalRequestStep.class);

        // Arrange: second approval step
        ApprovalRequestStep step2 = mock(ApprovalRequestStep.class);

        when(approvalRequestStepService.getByRequestId(1L))
                .thenReturn(List.of(step1, step2));

        when(step1.getId()).thenReturn(11L);
        when(step1.getStatus()).thenReturn("PENDING", "APPROVED");
        when(step1.getFlowStep()).thenReturn(flowStep);


        // Act
        ApprovalRequest result = approvalRequestService.approve(
                "APR-001",
                "user-001",
                "Looks good"
        );

        // Assert: only the first step is acted on
        verify(step1).setApproverId("user-001");
        verify(step1).setActionAt(any(LocalDateTime.class));
        verify(step1).setComment("Looks good");
        verify(step1).setStatus("APPROVED");
        verify(step2, never()).setStatus("APPROVED");

        verify(approvalRequestStepService).update(11L, step1);

        verify(request, never()).setStatus("APPROVED");

        assertSame(request, result);
    }

    @Test
    void approve_whenFinalPendingStepIsApproved_marksRequestApproved() {
        // Arrange: pending approval request
        ApprovalRequest request = mock(ApprovalRequest.class);

        ApprovalFlowStep flowStep = mock(ApprovalFlowStep.class);

        when(flowStep.getApproverType()).thenReturn("USER");
        when(flowStep.getApproverValue()).thenReturn("user-002");

        when(request.getId()).thenReturn(1L);
        when(request.getStatus()).thenReturn("PENDING");
        when(request.isDeleted()).thenReturn(false);
        when(request.getModNo()).thenReturn(0L);

        when(approvalRequestRepository.findByRequestCode("APR-002"))
                .thenReturn(Optional.of(request));
        when(approvalRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any(ApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Arrange: first step is already approved; second is pending
        ApprovalRequestStep step1 = mock(ApprovalRequestStep.class);
        when(step1.getStatus()).thenReturn("APPROVED");

        ApprovalRequestStep step2 = mock(ApprovalRequestStep.class);
        when(step2.getId()).thenReturn(12L);
        when(step2.getStatus()).thenReturn("PENDING", "APPROVED");
        when(step2.getFlowStep()).thenReturn(flowStep);

        when(approvalRequestStepService.getByRequestId(1L))
                .thenReturn(List.of(step1, step2));

        // Act
        ApprovalRequest result = approvalRequestService.approve(
                "APR-002",
                "user-002",
                "Final approval"
        );

        // Assert: the final step is approved
        verify(step2).setApproverId("user-002");
        verify(step2).setActionAt(any(LocalDateTime.class));
        verify(step2).setComment("Final approval");
        verify(step2).setStatus("APPROVED");

        verify(approvalRequestStepService).update(12L, step2);

        // The overall request should now be approved
        verify(request).setStatus("APPROVED");

        assertSame(request, result);
    }

    @Test
    void reject_whenPendingStepExists_marksRequestRejected() {
        // Arrange: pending approval request
        ApprovalRequest request = mock(ApprovalRequest.class);

        when(request.getId()).thenReturn(1L);
        when(request.getStatus()).thenReturn("PENDING");
        when(request.isDeleted()).thenReturn(false);
        when(request.getModNo()).thenReturn(0L);

        when(approvalRequestRepository.findByRequestCode("APR-003"))
                .thenReturn(Optional.of(request));
        when(approvalRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any(ApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Arrange: pending approval step
        ApprovalRequestStep step = mock(ApprovalRequestStep.class);
        ApprovalFlowStep flowStep = mock(ApprovalFlowStep.class);

        when(step.getId()).thenReturn(13L);
        when(step.getStatus()).thenReturn("PENDING");
        when(step.getFlowStep()).thenReturn(flowStep);

        when(flowStep.getApproverType()).thenReturn("USER");
        when(flowStep.getApproverValue()).thenReturn("user-003");

        when(approvalRequestStepService.getByRequestId(1L))
                .thenReturn(List.of(step));

        // Act
        ApprovalRequest result = approvalRequestService.reject(
                "APR-003",
                "user-003",
                "Request does not meet requirements"
        );

        // Assert: step is rejected
        verify(step).setApproverId("user-003");
        verify(step).setActionAt(any(LocalDateTime.class));
        verify(step).setComment("Request does not meet requirements");
        verify(step).setStatus("REJECTED");

        verify(approvalRequestStepService).update(13L, step);

        // Overall request is rejected
        verify(request).setStatus("REJECTED");

        assertSame(request, result);
    }

    @Test
    void approve_whenRequestAlreadyApproved_throwsException() {
        // Arrange
        ApprovalRequest request = mock(ApprovalRequest.class);

        when(request.getStatus()).thenReturn("APPROVED");

        when(approvalRequestRepository.findByRequestCode("APR-004"))
                .thenReturn(Optional.of(request));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvalRequestService.approve(
                        "APR-004",
                        "user-004",
                        "Trying to approve again"
                )
        );

        assertEquals(
                "Only pending approval requests can be acted on",
                exception.getMessage()
        );

        // No approval steps should be processed
        verify(approvalRequestStepService, never())
                .getByRequestId(any(Long.class));
    }

    @Test
    void reject_whenRequestAlreadyRejected_throwsException() {
        // Arrange
        ApprovalRequest request = mock(ApprovalRequest.class);

        when(request.getStatus()).thenReturn("REJECTED");

        when(approvalRequestRepository.findByRequestCode("APR-005"))
                .thenReturn(Optional.of(request));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvalRequestService.reject(
                        "APR-005",
                        "user-005",
                        "Trying to reject again"
                )
        );

        assertEquals(
                "Only pending approval requests can be acted on",
                exception.getMessage()
        );

        verify(approvalRequestStepService, never())
                .getByRequestId(any(Long.class));
    }

    @Test
    void approve_whenUserIsNotAuthorized_throwsException() {
        // Arrange
        ApprovalRequest request = mock(ApprovalRequest.class);
        ApprovalRequestStep currentStep = mock(ApprovalRequestStep.class);
        ApprovalFlowStep flowStep = mock(ApprovalFlowStep.class);

        when(request.getId()).thenReturn(1L);
        when(request.getStatus()).thenReturn("PENDING");

        when(approvalRequestRepository.findByRequestCode("APR-006"))
                .thenReturn(Optional.of(request));

        when(approvalRequestStepService.getByRequestId(1L))
                .thenReturn(List.of(currentStep));

        when(currentStep.getStatus()).thenReturn("PENDING");
        when(currentStep.getFlowStep()).thenReturn(flowStep);

        when(flowStep.getApproverType()).thenReturn("USER");
        when(flowStep.getApproverValue()).thenReturn("manager-001");

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvalRequestService.approve(
                        "APR-006",
                        "user-002",
                        "Trying unauthorized approval"
                )
        );

        assertEquals(
                "You are not authorized to act on this approval step",
                exception.getMessage()
        );

        // Verify no approval changes were made
        verify(currentStep, never()).setStatus(anyString());
        verify(approvalRequestStepService, never())
                .update(any(Long.class), any(ApprovalRequestStep.class));
        verify(approvalRequestRepository, never()).save(any());
    }

    @Test
    void approve_whenUserIsAuthorized_processesApproval() {
        // Arrange
        ApprovalRequest request = mock(ApprovalRequest.class);
        ApprovalRequestStep currentStep = mock(ApprovalRequestStep.class);
        ApprovalFlowStep flowStep = mock(ApprovalFlowStep.class);

        when(request.getId()).thenReturn(1L);
        when(request.getStatus()).thenReturn("PENDING");

        when(approvalRequestRepository.findByRequestCode("APR-007"))
                .thenReturn(Optional.of(request));

        when(approvalRequestStepService.getByRequestId(1L))
                .thenReturn(List.of(currentStep));

        when(request.getModNo()).thenReturn(0L);

        when(approvalRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(currentStep.getId()).thenReturn(10L);
        when(currentStep.getStatus()).thenReturn("PENDING", "APPROVED");
        when(currentStep.getFlowStep()).thenReturn(flowStep);

        when(flowStep.getApproverType()).thenReturn("USER");
        when(flowStep.getApproverValue()).thenReturn("manager-001");

        // Act
        approvalRequestService.approve(
                "APR-007",
                "manager-001",
                "Approved"
        );

        // Assert
        verify(currentStep).setApproverId("manager-001");
        verify(currentStep).setStatus("APPROVED");
        verify(approvalRequestStepService)
                .update(10L, currentStep);
        verify(approvalRequestRepository).save(request);
    }

    @Test
    void approve_whenApproverTypeIsUnsupported_throwsException() {
        // Arrange
        ApprovalRequest request = mock(ApprovalRequest.class);
        ApprovalRequestStep currentStep = mock(ApprovalRequestStep.class);
        ApprovalFlowStep flowStep = mock(ApprovalFlowStep.class);

        when(request.getId()).thenReturn(1L);
        when(request.getStatus()).thenReturn("PENDING");

        when(approvalRequestRepository.findByRequestCode("APR-008"))
                .thenReturn(Optional.of(request));

        when(approvalRequestStepService.getByRequestId(1L))
                .thenReturn(List.of(currentStep));

        when(currentStep.getStatus()).thenReturn("PENDING");
        when(currentStep.getFlowStep()).thenReturn(flowStep);

        when(flowStep.getApproverType()).thenReturn("ROLE");

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvalRequestService.approve(
                        "APR-008",
                        "manager-001",
                        "Trying unsupported approver type"
                )
        );

        assertEquals(
                "Unsupported approver type: ROLE",
                exception.getMessage()
        );

        // No approval changes should be made
        verify(currentStep, never()).setApproverId(anyString());
        verify(currentStep, never()).setStatus(anyString());

        verify(approvalRequestStepService, never())
                .update(any(Long.class), any(ApprovalRequestStep.class));

        verify(approvalRequestRepository, never()).save(any());
    }

    @Test
    void reject_whenRequestAlreadyApproved_throwsException() {
        ApprovalRequest request = mock(ApprovalRequest.class);
        when(request.getStatus()).thenReturn("APPROVED");

        when(approvalRequestRepository.findByRequestCode("APR-001"))
                .thenReturn(Optional.of(request));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvalRequestService.reject(
                        "APR-001", "USER-001", "Trying to reject approved request"
                )
        );

        assertEquals(
                "Only pending approval requests can be acted on",
                exception.getMessage()
        );

        verify(approvalRequestStepService, never()).getByRequestId(anyLong());
    }

    @Test
    void approve_whenRequestAlreadyRejected_throwsException() {
        ApprovalRequest request = mock(ApprovalRequest.class);
        when(request.getStatus()).thenReturn("REJECTED");

        when(approvalRequestRepository.findByRequestCode("APR-001"))
                .thenReturn(Optional.of(request));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> approvalRequestService.approve(
                        "APR-001", "USER-001", "Trying to approve rejected request"
                )
        );

        assertEquals(
                "Only pending approval requests can be acted on",
                exception.getMessage()
        );

        verify(approvalRequestStepService, never()).getByRequestId(anyLong());
    }

}