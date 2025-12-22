package com.hfits.system.core.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 迁移请求
 *
 * @author hfits
 */
public class MigrationRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Integer VALIDATE_LIMIT = 100;

    /**
     * 源表名
     */
    private String tableName;

    /**
     * 实体类全限定名
     */
    private String entityClassName;

    /**
     * Mapper接口全限定名
     */
    private String mapperClassName;

    /**
     * Mapper方法名
     */
    private String methodName;

    /**
     * 字段映射列表
     */
    private List<FieldMapping> fieldMappings;

    /**
     * 验证时查询的数据条数（默认10）
     */
    private Integer validateLimit;

    public MigrationRequest() {
        this.validateLimit = VALIDATE_LIMIT;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public String getMapperClassName() {
        return mapperClassName;
    }

    public void setMapperClassName(String mapperClassName) {
        this.mapperClassName = mapperClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public void setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    public Integer getValidateLimit() {
        return validateLimit;
    }

    public void setValidateLimit(Integer validateLimit) {
        this.validateLimit = validateLimit;
    }
}
