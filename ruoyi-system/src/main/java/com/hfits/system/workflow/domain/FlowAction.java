package com.hfits.system.workflow.domain;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 审批动作记录
 */
public class FlowAction extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "实例ID不能为空")
    private Long instanceId;

    /** 动作发生的步骤 */
    private String stepCode;

    /** PASS / REJECT  */
    private String action;

    /** 审批意见 */
    private String comment;

    /** 审批人 */
    private Long operatorId;

    private Date actionTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(Long instanceId)
    {
        this.instanceId = instanceId;
    }

    public String getStepCode()
    {
        return stepCode;
    }

    public void setStepCode(String stepCode)
    {
        this.stepCode = stepCode;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public Date getActionTime()
    {
        return actionTime;
    }

    public void setActionTime(Date actionTime)
    {
        this.actionTime = actionTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("instanceId", getInstanceId())
                .append("stepCode", getStepCode())
                .append("action", getAction())
                .append("comment", getComment())
                .append("operatorId", getOperatorId())
                .append("actionTime", getActionTime())
                .toString();
    }
}

