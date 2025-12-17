package com.ruoyi.system.domain.vo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 *
 */
public class ApiStatis implements Serializable {
    private static final long serialVersionUID = 1L;

    private String apiKey;

    private Long totalCount;

    private Double avgResponseTime;


    private Double recent100AvgResponseTime;

    private Long errorCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCallTime;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(Double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }


    public Double getRecent100AvgResponseTime() {
        return recent100AvgResponseTime;
    }

    public void setRecent100AvgResponseTime(Double recent100AvgResponseTime) {
        this.recent100AvgResponseTime = recent100AvgResponseTime;
    }

    public Date getLastCallTime() {
        return lastCallTime;
    }

    public void setLastCallTime(Date lastCallTime) {
        this.lastCallTime = lastCallTime;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }
}
