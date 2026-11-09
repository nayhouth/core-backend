package com.corebackend.approval.dto;

import com.corebackend.approval.entity.ApprovalRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalRequestSubmissionResultDto {

    private boolean approvalRequired;
    private ApprovalRequest approvalRequest;
    private String message;
}