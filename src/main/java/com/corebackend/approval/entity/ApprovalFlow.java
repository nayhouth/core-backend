package com.corebackend.approval.entity;

import com.corebackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "core_approval_flow")
public class ApprovalFlow extends BaseEntity {

    @Column(name = "flow_code", nullable = false, unique = true, length = 100)
    private String flowCode;

    @Column(name = "flow_name", nullable = false, length = 200)
    private String flowName;

    @Column(name = "object_type", nullable = false, length = 100)
    private String objectType;

    @Column(nullable = false, length = 100)
    private String operation;

    @Column(name = "condition_type", nullable = false, length = 50)
    private String conditionType;

    @Column(name = "condition_config", columnDefinition = "jsonb")
    private String conditionConfig;

    @Column(nullable = false)
    private Integer priority = 100;

    @Column(nullable = false, length = 1)
    private String active = "Y";

    @Column(name = "workspace_id")
    private Long workspaceId;
}