package com.corebackend.approval.service;

import com.corebackend.approval.entity.ApprovalContext;
import com.corebackend.approval.entity.ApprovalFlow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalConditionEvaluatorTest {

    private ApprovalConditionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ApprovalConditionEvaluator(new ObjectMapper());
    }

    @Test
    void alwaysConditionMatchesSameObjectAndOperation() {
        ApprovalFlow flow = flow("ALWAYS", null);
        ApprovalContext context = context("TASK", "STATUS_CHANGE",
                "IN_PROGRESS", "DONE");

        assertTrue(evaluator.matches(flow, context));
    }

    @Test
    void statusTransitionMatchesConfiguredStatuses() {
        ApprovalFlow flow = flow(
                "STATUS_TRANSITION",
                """
                {"fromStatus":"IN_PROGRESS","toStatus":"DONE"}
                """
        );

        ApprovalContext context = context("TASK", "STATUS_CHANGE",
                "IN_PROGRESS", "DONE");

        assertTrue(evaluator.matches(flow, context));
    }

    @Test
    void statusTransitionDoesNotMatchDifferentTarget() {
        ApprovalFlow flow = flow(
                "STATUS_TRANSITION",
                """
                {"fromStatus":"IN_PROGRESS","toStatus":"DONE"}
                """
        );

        ApprovalContext context = context("TASK", "STATUS_CHANGE",
                "IN_PROGRESS", "TESTING");

        assertFalse(evaluator.matches(flow, context));
    }

    @Test
    void differentObjectTypeDoesNotMatch() {
        ApprovalFlow flow = flow("ALWAYS", null);
        ApprovalContext context = context("WORKSPACE", "STATUS_CHANGE",
                "IN_PROGRESS", "DONE");

        assertFalse(evaluator.matches(flow, context));
    }

    private ApprovalFlow flow(String conditionType, String config) {
        ApprovalFlow flow = new ApprovalFlow();
        flow.setObjectType("TASK");
        flow.setOperation("STATUS_CHANGE");
        flow.setConditionType(conditionType);
        flow.setConditionConfig(config);
        flow.setFlowCode("TEST_FLOW");
        return flow;
    }

    private ApprovalContext context(
            String objectType,
            String operation,
            String fromStatus,
            String toStatus) {

        ApprovalContext context = new ApprovalContext();
        context.setObjectType(objectType);
        context.setOperation(operation);
        context.setFromStatus(fromStatus);
        context.setToStatus(toStatus);
        return context;
    }

    @Test
    void validateAlwaysCondition_allowsNullConfig() {
        ApprovalFlow flow = flow("ALWAYS", null);

        assertDoesNotThrow(() ->
                evaluator.validateConditionConfig(flow));
    }

    @Test
    void validateStatusTransition_acceptsValidConfig() {
        ApprovalFlow flow = flow(
                "STATUS_TRANSITION",
                """
                {"fromStatus":"PENDING","toStatus":"APPROVED"}
                """
        );

        assertDoesNotThrow(() ->
                evaluator.validateConditionConfig(flow));
    }

    @Test
    void validateStatusTransition_rejectsNullConfig() {
        ApprovalFlow flow = flow("STATUS_TRANSITION", null);

        assertThrows(IllegalArgumentException.class, () ->
                evaluator.validateConditionConfig(flow));
    }

    @Test
    void validateStatusTransition_rejectsMalformedJson() {
        ApprovalFlow flow = flow(
                "STATUS_TRANSITION",
                """
                {"fromStatus":"PENDING","toStatus":
                """
        );

        assertThrows(IllegalArgumentException.class, () ->
                evaluator.validateConditionConfig(flow));
    }

    @Test
    void validateStatusTransition_rejectsMissingFromStatus() {
        ApprovalFlow flow = flow(
                "STATUS_TRANSITION",
                """
                {"toStatus":"APPROVED"}
                """
        );

        assertThrows(IllegalArgumentException.class, () ->
                evaluator.validateConditionConfig(flow));
    }

    @Test
    void validateStatusTransition_rejectsBlankToStatus() {
        ApprovalFlow flow = flow(
                "STATUS_TRANSITION",
                """
                {"fromStatus":"PENDING","toStatus":" "}
                """
        );

        assertThrows(IllegalArgumentException.class, () ->
                evaluator.validateConditionConfig(flow));
    }

    @Test
    void validateStatusTransition_rejectsJsonArray() {
        ApprovalFlow flow = flow(
                "STATUS_TRANSITION",
                """
                ["PENDING","APPROVED"]
                """
        );

        assertThrows(IllegalArgumentException.class, () ->
                evaluator.validateConditionConfig(flow));
    }

    @Test
    void validateCondition_rejectsUnsupportedType() {
        ApprovalFlow flow = flow("UNKNOWN", null);

        assertThrows(IllegalArgumentException.class, () ->
                evaluator.validateConditionConfig(flow));
    }
}