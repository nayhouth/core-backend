
package com.corebackend.approval.entity;

import com.corebackend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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

    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionConfig() {
        return conditionConfig;
    }

    public void setConditionConfig(String conditionConfig) {
        this.conditionConfig = conditionConfig;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
