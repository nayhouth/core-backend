package com.corebackend.approval.service;

import com.corebackend.approval.entity.ApprovalFlowStep;
import com.corebackend.common.service.BaseService;

import java.util.List;

public interface ApprovalFlowStepService
        extends BaseService<ApprovalFlowStep> {

    List<ApprovalFlowStep> getByFlowId(Long flowId);

}