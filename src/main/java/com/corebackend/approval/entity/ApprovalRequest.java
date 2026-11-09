package com.corebackend.approval.entity;

import com.corebackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "core_approval_request")
public class ApprovalRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_id", nullable = false)
    private ApprovalFlow flow;

    @Column(name = "request_code", nullable = false, unique = true, length = 100)
    private String requestCode;

    @Column(name = "object_type", nullable = false, length = 100)
    private String objectType;

    @Column(name = "object_id", nullable = false, length = 100)
    private String objectId;

    @Column(nullable = false, length = 100)
    private String operation;

    @Column(name = "requester_id", nullable = false, length = 100)
    private String requesterId;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "request_data", columnDefinition = "jsonb")
    private String requestData;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
}