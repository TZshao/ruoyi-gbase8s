package com.hfits.system.core.domain;

import com.hfits.common.annotation.Excel;
import com.hfits.common.annotation.Excel.ColumnType;
import com.hfits.common.core.domain.BaseEntity;

/**
 * API接口记录表 api_Log
 *
 * @author hfits
 */
public class ApiLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Excel(name = "序号", cellType = ColumnType.NUMERIC)
    private Long id;

    /**
     * 接口标识（格式：HTTP方法 + URI，如 "GET /flow/instance/list"）
     */
    @Excel(name = "接口标识")
    private String apiKey;

    /**
     * HTTP方法（GET、POST等）
     */
    @Excel(name = "HTTP方法")
    private String httpMethod;

    /**
     * 请求URI
     */
    @Excel(name = "请求URI")
    private String requestUri;

    /**
     * 响应时长（毫秒）
     */
    @Excel(name = "响应时长", suffix = "毫秒")
    private Long responseTime;

    /**
     * 状态（0正常 1异常）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private String status;

    /**
     * 错误消息
     */
    @Excel(name = "错误消息")
    private String errorMsg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}

