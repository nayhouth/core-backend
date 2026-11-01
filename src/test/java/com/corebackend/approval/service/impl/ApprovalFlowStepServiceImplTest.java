package com.corebackend.approval.service.impl;

import com.corebackend.approval.entity.ApprovalFlow;
import com.corebackend.approval.entity.ApprovalFlowStep;
import com.corebackend.approval.repository.ApprovalFlowStepRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ApprovalFlowStepServiceImplTest {

    @Mock
    private ApprovalFlowStepRepository approvalFlowStepRepository;

    @InjectMocks
    private ApprovalFlowStepServiceImpl approvalFlowStepService;

    @Test
    void create_whenStepOrderIsInvalid_throwsException() {
        ApprovalFlowStep step = new ApprovalFlowStep();
        step.setFlow(createFlow(1L));
        step.setStepOrder(0);
        step.setApproverType("USER");
        step.setApproverValue("user-001");

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalFlowStepService.create(step)
        );

        verify(approvalFlowStepRepository, never()).save(any());
    }

    @Test
    void create_whenStepOrderAlreadyExists_throwsException() {
        ApprovalFlowStep step = new ApprovalFlowStep();
        step.setFlow(createFlow(1L));
        step.setStepOrder(1);
        step.setApproverType("USER");
        step.setApproverValue("user-001");

        when(approvalFlowStepRepository
                .existsByFlowIdAndStepOrderAndIsDeletedFalse(1L, 1))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalFlowStepService.create(step)
        );

        verify(approvalFlowStepRepository, never()).save(any());
    }

    @Test
    void create_whenValid_normalizesApproverType() {
        ApprovalFlowStep step = new ApprovalFlowStep();
        step.setFlow(createFlow(1L));
        step.setStepOrder(1);
        step.setApproverType(" user ");
        step.setApproverValue(" user-001 ");

        when(approvalFlowStepRepository
                .existsByFlowIdAndStepOrderAndIsDeletedFalse(1L, 1))
                .thenReturn(false);

        when(approvalFlowStepRepository.save(any(ApprovalFlowStep.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalFlowStep created = approvalFlowStepService.create(step);

        assertEquals("USER", created.getApproverType());
        assertEquals("user-001", created.getApproverValue());
        verify(approvalFlowStepRepository).save(step);
    }

    private ApprovalFlow createFlow(Long id) {
        ApprovalFlow flow = mock(ApprovalFlow.class);
        when(flow.getId()).thenReturn(id);
        return flow;
    }

    @Test
    void create_whenApproverTypeIsRole_throwsException() {
        ApprovalFlowStep step = new ApprovalFlowStep();
        step.setFlow(createFlow(1L));
        step.setStepOrder(1);
        step.setApproverType("ROLE");
        step.setApproverValue("MANAGER");

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalFlowStepService.create(step)
        );

        verify(approvalFlowStepRepository, never()).save(any());
    }

    @Test
    void create_whenApproverTypeIsUnsupported_throwsException() {
        ApprovalFlowStep step = new ApprovalFlowStep();
        step.setFlow(createFlow(1L));
        step.setStepOrder(1);
        step.setApproverType("DEPARTMENT");
        step.setApproverValue("FINANCE");

        assertThrows(
                IllegalArgumentException.class,
                () -> approvalFlowStepService.create(step)
        );

        verify(approvalFlowStepRepository, never()).save(any());
    }

    @Test
    void update_whenStepOrderIsUnchanged_allowsUpdate() {
        ApprovalFlow flow = createFlow(1L);

        ApprovalFlowStep input = new ApprovalFlowStep();
        input.setFlow(flow);
        input.setStepOrder(1);
        input.setApproverType("USER");
        input.setApproverValue("user-002");

        ApprovalFlowStep existing = new ApprovalFlowStep();
        existing.setFlow(flow);
        existing.setStepOrder(1);
        existing.setApproverType("USER");
        existing.setApproverValue("user-001");
        existing.setModNo(0L);

        when(approvalFlowStepRepository
                .existsByFlowIdAndStepOrderAndIsDeletedFalseAndIdNot(1L, 1, 10L))
                .thenReturn(false);

        when(approvalFlowStepRepository.findById(10L))
                .thenReturn(Optional.of(existing));

        when(approvalFlowStepRepository.save(any(ApprovalFlowStep.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalFlowStep updated = approvalFlowStepService.update(10L, input);

        assertNotNull(updated);
        assertEquals("user-002", updated.getApproverValue());

        verify(approvalFlowStepRepository).save(existing);
    }

}