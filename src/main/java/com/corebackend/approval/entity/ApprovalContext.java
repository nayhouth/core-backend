package com.corebackend.approval.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalContext {

    private String objectType;
    private String operation;
    private String fromStatus;
    private String toStatus;
}