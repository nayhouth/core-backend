package com.corebackend.approval.service;

import com.corebackend.approval.entity.ApprovalRequest;
import com.corebackend.common.service.BaseService;
import com.corebackend.approval.dto.ApprovalRequestSubmissionDto;
import com.corebackend.approval.dto.ApprovalRequestSubmissionResultDto;
import java.util.List;
import java.util.Optional;

public interface ApprovalRequestService extends BaseService<ApprovalRequest> {

    Optional<ApprovalRequest> getByRequestCode(String requestCode);

    List<ApprovalRequest> getByRequesterAndStatus(
            String requesterId,
            String status
    );

    List<ApprovalRequest> getByObjectAndOperationAndStatus(
            String objectType,
            String objectId,
            String operation,
            String status
    );

    ApprovalRequestSubmissionResultDto submit(
            ApprovalRequestSubmissionDto submission
    );

    ApprovalRequest approve(
            String requestCode,
            String approverId,
            String comment
    );

    ApprovalRequest reject(
            String requestCode,
            String approverId,
            String comment
    );

}