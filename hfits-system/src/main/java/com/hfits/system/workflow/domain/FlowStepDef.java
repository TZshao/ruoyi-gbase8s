package com.hfits.system.workflow.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.hfits.common.core.domain.BaseEntity;

/**
 * 流程步骤定义
 */
public class FlowStepDef extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "flowId不能为空")
    private Long flowId;
    private Integer flowVersion;

    @NotBlank(message = "步骤编码不能为空")
    private String stepCode;

    @NotBlank(message = "步骤名称不能为空")
    private String stepName;

    private String nextOnPass;

    private String nextOnReject;

    /** 审批人类型（角色/用户/部门负责人等） */
    private String handlerType;

    /** 审批人取值 */
    private String handlerValue;

    /** 通过事件 */
    private String eventOnPass;

    /** 拒绝事件 */
    private String eventOnReject;

    /** 排序 */
    private Integer orderNum;

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

    public String getStepCode()
    {
        return stepCode;
    }

    public void setStepCode(String stepCode)
    {
        this.stepCode = stepCode;
    }

    public String getStepName()
    {
        return stepName;
    }

    public void setStepName(String stepName)
    {
        this.stepName = stepName;
    }

    public String getNextOnPass()
    {
        return nextOnPass;
    }

    public void setNextOnPass(String nextOnPass)
    {
        this.nextOnPass = nextOnPass;
    }

    public String getNextOnReject()
    {
        return nextOnReject;
    }

    public void setNextOnReject(String nextOnReject)
    {
        this.nextOnReject = nextOnReject;
    }

    public String getHandlerType()
    {
        return handlerType;
    }

    public void setHandlerType(String handlerType)
    {
        this.handlerType = handlerType;
    }

    public String getHandlerValue()
    {
        return handlerValue;
    }

    public void setHandlerValue(String handlerValue)
    {
        this.handlerValue = handlerValue;
    }

    public String getEventOnPass()
    {
        return eventOnPass;
    }

    public void setEventOnPass(String eventOnPass)
    {
        this.eventOnPass = eventOnPass;
    }

    public String getEventOnReject()
    {
        return eventOnReject;
    }

    public void setEventOnReject(String eventOnReject)
    {
        this.eventOnReject = eventOnReject;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("flowId", getFlowId())
                .append("stepCode", getStepCode())
                .append("stepName", getStepName())
                .append("nextOnPass", getNextOnPass())
                .append("nextOnReject", getNextOnReject())
                .append("handlerType", getHandlerType())
                .append("handlerValue", getHandlerValue())
                .append("eventOnPass", getEventOnPass())
                .append("eventOnReject", getEventOnReject())
                .append("orderNum", getOrderNum())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

