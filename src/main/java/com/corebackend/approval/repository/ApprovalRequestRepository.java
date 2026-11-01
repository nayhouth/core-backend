package com.corebackend.approval.repository;

import com.corebackend.approval.entity.ApprovalRequest;
import com.corebackend.common.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalRequestRepository
        extends BaseRepository<ApprovalRequest> {

    Optional<ApprovalRequest> findByRequestCode(String requestCode);

    List<ApprovalRequest> findByRequesterIdAndStatusOrderBySubmittedAtDesc(
            String requesterId,
            String status
    );

    List<ApprovalRequest> findByObjectTypeAndObjectIdAndOperationAndStatus(
            String objectType,
            String objectId,
            String operation,
            String status
    );
}