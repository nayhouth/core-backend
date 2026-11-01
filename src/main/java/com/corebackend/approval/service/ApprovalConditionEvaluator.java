package com.corebackend.approval.service;

import com.corebackend.approval.entity.ApprovalContext;
import com.corebackend.approval.entity.ApprovalFlow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ApprovalConditionEvaluator {

    private final ObjectMapper objectMapper;

    public ApprovalConditionEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validateConditionConfig(ApprovalFlow flow) {
        if (flow == null) {
            throw new IllegalArgumentException("Approval flow is required");
        }

        String conditionType = flow.getConditionType();

        if ("ALWAYS".equalsIgnoreCase(conditionType)) {
            return;
        }

        if ("STATUS_TRANSITION".equalsIgnoreCase(conditionType)) {
            JsonNode config = parseConfig(flow);

            if (!isValidStatus(config, "fromStatus")
                    || !isValidStatus(config, "toStatus")) {
                throw new IllegalArgumentException(
                        "STATUS_TRANSITION requires nonblank fromStatus and toStatus "
                                + "in conditionConfig for flow: " + flow.getFlowCode()
                );
            }

            return;
        }

        throw new IllegalArgumentException(
                "Unsupported condition type: " + conditionType
        );
    }

    public boolean matches(
            ApprovalFlow flow,
            ApprovalContext context) {

        if (flow == null || context == null) {
            return false;
        }

        if (!equalsIgnoreCase(flow.getObjectType(), context.getObjectType())
                || !equalsIgnoreCase(flow.getOperation(), context.getOperation())) {
            return false;
        }

        if ("ALWAYS".equalsIgnoreCase(flow.getConditionType())) {
            return true;
        }

        if ("STATUS_TRANSITION".equalsIgnoreCase(flow.getConditionType())) {
            return matchesStatusTransition(flow, context);
        }

        return false;
    }

    private boolean matchesStatusTransition(
            ApprovalFlow flow,
            ApprovalContext context) {

        JsonNode config = parseConfig(flow);

        String expectedFrom = text(config, "fromStatus");
        String expectedTo = text(config, "toStatus");

        return expectedFrom != null
                && expectedTo != null
                && expectedFrom.equals(context.getFromStatus())
                && expectedTo.equals(context.getToStatus());
    }

    private JsonNode parseConfig(ApprovalFlow flow) {
        if (flow.getConditionConfig() == null
                || flow.getConditionConfig().isBlank()) {
            throw new IllegalArgumentException(
                    "conditionConfig is required for flow: " + flow.getFlowCode()
            );
        }

        try {
            JsonNode config = objectMapper.readTree(flow.getConditionConfig());

            if (config == null || !config.isObject()) {
                throw new IllegalArgumentException(
                        "conditionConfig must be a JSON object for flow: "
                                + flow.getFlowCode()
                );
            }

            return config;

        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid conditionConfig for flow: " + flow.getFlowCode(),
                    exception
            );
        }
    }

    private boolean isValidStatus(JsonNode node, String field) {
        JsonNode value = node.get(field);

        return value != null
                && value.isTextual()
                && !value.asText().isBlank();
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);

        return value != null && value.isTextual()
                ? value.asText()
                : null;
    }

    private boolean equalsIgnoreCase(String first, String second) {
        return first != null
                && second != null
                && first.equalsIgnoreCase(second);
    }
}