package com.corebackend.approval.entity;

import com.corebackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

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

    public ApprovalFlow getFlow() {
        return flow;
    }

    public void setFlow(ApprovalFlow flow) {
        this.flow = flow;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}