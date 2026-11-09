package com.corebackend.approval.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalRequestSubmissionDto {

    private String objectType;
    private String operation;
    private String fromStatus;
    private String toStatus;
    private String objectId;
    private String requesterId;
    private String requestData;
}