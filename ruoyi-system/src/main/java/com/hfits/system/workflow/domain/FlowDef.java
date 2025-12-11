package com.hfits.system.workflow.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程定义
 */
public class FlowDef extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 64, message = "流程编号不能超过64个字符")
    private String flowCode;

    @NotBlank(message = "流程名称不能为空")
    @Size(max = 128, message = "流程名称不能超过128个字符")
    private String flowName;

    /** JSON schema 字符串 */
    private String formSchema;

    /** 版本号 */
    private Integer version;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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

    public String getFormSchema()
    {
        return formSchema;
    }

    public void setFormSchema(String formSchema)
    {
        this.formSchema = formSchema;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("flowCode", getFlowCode())
                .append("flowName", getFlowName())
                .append("formSchema", getFormSchema())
                .append("version", getVersion())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

