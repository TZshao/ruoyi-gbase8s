package com.hfits.system.workflow.domain;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程实例
 */
public class FlowInstance extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "流程ID不能为空")
    private Long flowId;
    private Integer flowVersion;

    /** 申请人用户ID */
    private Long applicantId;

    /** 状态 PENDING/RUNNING/CLOSED */
    private String status;

    /** 当前步骤编码 */
    private String currentStepCode;

    /** 业务表单数据(JSON) */
    private String formData;

    // 关联展示字段
    private String flowCode;
    private String flowName;

    public Integer getFlowVersion() {
        return flowVersion;
    }

    public void setFlowVersion(Integer flowVersion) {
        this.flowVersion = flowVersion;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getFlowId()
    {
        return flowId;
    }

    public void setFlowId(Long flowId)
    {
        this.flowId = flowId;
    }

    public Long getApplicantId()
    {
        return applicantId;
    }

    public void setApplicantId(Long applicantId)
    {
        this.applicantId = applicantId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getCurrentStepCode()
    {
        return currentStepCode;
    }

    public void setCurrentStepCode(String currentStepCode)
    {
        this.currentStepCode = currentStepCode;
    }

    public String getFormData()
    {
        return formData;
    }

    public void setFormData(String formData)
    {
        this.formData = formData;
    }

    public String getFlowCode()
    {
        return flowCode;
    }

    public void setFlowCode(String flowCode)
    {
        this.flowCode = flowCode;
    }

    public String getFlowName()
    {
        return flowName;
    }

    public void setFlowName(String flowName)
    {
        this.flowName = flowName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("flowId", getFlowId())
                .append("applicantId", getApplicantId())
                .append("status", getStatus())
                .append("currentStepCode", getCurrentStepCode())
                .append("formData", getFormData())
                .append("flowCode", getFlowCode())
                .append("flowName", getFlowName())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

