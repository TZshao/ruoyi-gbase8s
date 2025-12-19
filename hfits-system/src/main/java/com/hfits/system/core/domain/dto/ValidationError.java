package com.hfits.system.core.domain.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * 单条验证错误信息
 *
 * @author hfits
 */
public class ValidationError implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 行号（从1开始） */
    private Integer rowNumber;

    /** 源数据 */
    private Map<String, Object> sourceData;

    /** 错误原因 */
    private String errorMessage;

    /** 异常类型 */
    private String exceptionType;

    public ValidationError() {
    }

    public ValidationError(Integer rowNumber, Map<String, Object> sourceData, String errorMessage, String exceptionType) {
        this.rowNumber = rowNumber;
        this.sourceData = sourceData;
        this.errorMessage = errorMessage;
        this.exceptionType = exceptionType;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Map<String, Object> getSourceData() {
        return sourceData;
    }

    public void setSourceData(Map<String, Object> sourceData) {
        this.sourceData = sourceData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }
}
