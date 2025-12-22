package com.hfits.system.workflow.domain;

import com.hfits.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 流程定义
 */
public class FlowDef extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * PK
     */
    private Long id;

    /**
     * 流程编码
     */
    @NotBlank(message = "流程码不能为空")
    private String flowCode;

    /**
     * 流程名
     */
    @NotBlank(message = "流程名不能为空")
    private String flowName;

    /**
     * 表单定义JSON
     */
    private String formSchema;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 是否发布
     */
    private Boolean publish;

    /**
     * 是否删除
     */
    private Boolean isDel = false;

    public Boolean isPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public boolean isDel() {
        return isDel;
    }

    public void setDel(boolean del) {
        isDel = del;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getFormSchema() {
        return formSchema;
    }

    public void setFormSchema(String formSchema) {
        this.formSchema = formSchema;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("flowCode", getFlowCode())
                .append("flowName", getFlowName())
                .append("formSchema", getFormSchema())
                .append("version", getVersion())
                .append("publish", isPublish())
                .append("isDel", isDel())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

