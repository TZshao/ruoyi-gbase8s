package com.hfits.system.core.domain.dto;

import java.io.Serializable;

/**
 * Mapper方法信息
 *
 * @author hfits
 */
public class MapperMethodInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 方法名 */
    private String methodName;

    /** Mapper接口全限定名 */
    private String mapperClassName;

    /** 参数类型（实体类全限定名） */
    private String parameterType;

    /** 参数类型简单名 */
    private String parameterTypeSimpleName;

    public MapperMethodInfo() {
    }

    public MapperMethodInfo(String methodName, String mapperClassName, String parameterType, String parameterTypeSimpleName) {
        this.methodName = methodName;
        this.mapperClassName = mapperClassName;
        this.parameterType = parameterType;
        this.parameterTypeSimpleName = parameterTypeSimpleName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMapperClassName() {
        return mapperClassName;
    }

    public void setMapperClassName(String mapperClassName) {
        this.mapperClassName = mapperClassName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getParameterTypeSimpleName() {
        return parameterTypeSimpleName;
    }

    public void setParameterTypeSimpleName(String parameterTypeSimpleName) {
        this.parameterTypeSimpleName = parameterTypeSimpleName;
    }
}
