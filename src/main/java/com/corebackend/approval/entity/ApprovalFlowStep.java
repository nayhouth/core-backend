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

@Getter
@Setter
@Entity
@Table(name = "core_approval_flow_step")
public class ApprovalFlowStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_id", nullable = false)
    private ApprovalFlow flow;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "approver_type", nullable = false, length = 30)
    private String approverType;

    @Column(name = "approver_value", nullable = false, length = 200)
    private String approverValue;

    @Column(name = "step_name", length = 200)
    private String stepName;
}