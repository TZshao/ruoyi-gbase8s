package com.hfits.system.core.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 验证结果
 *
 * @author hfits
 */
public class ValidationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 是否通过 */
    private Boolean passed;

    /** 验证数据总数 */
    private Integer totalCount;

    /** 成功数量 */
    private Integer successCount;

    /** 失败数量 */
    private Integer failureCount;

    /** 失败记录列表 */
    private List<ValidationError> errors;

    /** 验证消息 */
    private String message;

    public ValidationResult() {
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
