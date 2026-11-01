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
@Table(name = "core_approval_request_step")
public class ApprovalRequestStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private ApprovalRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_step_id", nullable = false)
    private ApprovalFlowStep flowStep;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "approver_id", length = 100)
    private String approverId;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "action_at")
    private LocalDateTime actionAt;

    @Column(name = "comment", length = 1000)
    private String comment;

    public ApprovalRequest getRequest() {
        return request;
    }

    public void setRequest(ApprovalRequest request) {
        this.request = request;
    }

    public ApprovalFlowStep getFlowStep() {
        return flowStep;
    }

    public void setFlowStep(ApprovalFlowStep flowStep) {
        this.flowStep = flowStep;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getActionAt() {
        return actionAt;
    }

    public void setActionAt(LocalDateTime actionAt) {
        this.actionAt = actionAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}