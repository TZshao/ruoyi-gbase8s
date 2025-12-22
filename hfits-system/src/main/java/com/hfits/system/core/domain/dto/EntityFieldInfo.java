package com.hfits.system.core.domain.dto;

import java.io.Serializable;

/**
 * 实体类字段信息
 *
 * @author hfits
 */
public class EntityFieldInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 字段描述
     */
    private String description;

    public EntityFieldInfo() {
    }

    public EntityFieldInfo(String fieldName, String fieldType, Boolean required) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.required = required;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
