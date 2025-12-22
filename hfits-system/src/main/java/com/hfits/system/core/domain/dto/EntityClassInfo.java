package com.hfits.system.core.domain.dto;

import java.io.Serializable;

/**
 * 实体类信息
 *
 * @author hfits
 */
public class EntityClassInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 类名
     */
    private String className;

    /**
     * 全限定名
     */
    private String fullClassName;

    public EntityClassInfo() {
    }

    public EntityClassInfo(String className, String fullClassName) {
        this.className = className;
        this.fullClassName = fullClassName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }
}
