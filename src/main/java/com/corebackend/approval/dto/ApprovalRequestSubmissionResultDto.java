package com.corebackend.approval.dto;

import com.corebackend.approval.entity.ApprovalRequest;

public class ApprovalRequestSubmissionResultDto {

    private boolean approvalRequired;
    private ApprovalRequest approvalRequest;
    private String message;

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public ApprovalRequest getApprovalRequest() {
        return approvalRequest;
    }

    public void setApprovalRequest(ApprovalRequest approvalRequest) {
        this.approvalRequest = approvalRequest;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}