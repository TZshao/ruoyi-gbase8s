package com.hfits.system.core.domain.dto;

import java.io.Serializable;

/**
 * 字段映射关系
 *
 * @author hfits
 */
public class FieldMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 源表字段名
     */
    private String sourceField;

    /**
     * 目标实体类字段名
     */
    private String targetField;

    public FieldMapping() {
    }

    public FieldMapping(String sourceField, String targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }

    public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }
}
