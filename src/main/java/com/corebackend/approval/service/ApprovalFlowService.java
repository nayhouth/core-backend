package com.corebackend.approval.service;

import com.corebackend.approval.entity.ApprovalFlow;
import com.corebackend.common.service.BaseService;
import com.corebackend.approval.entity.ApprovalContext;
import java.util.List;
import java.util.Optional;

public interface ApprovalFlowService extends BaseService<ApprovalFlow> {

    Optional<ApprovalFlow> getByFlowCode(String flowCode);

    List<ApprovalFlow> getMatchingFlows(
            String objectType,
            String operation
    );

    List<ApprovalFlow> findMatchingFlows(ApprovalContext context);

}