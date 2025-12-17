package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.system.domain.ApiLog;
import com.ruoyi.system.domain.vo.ApiStatis;
import com.ruoyi.system.mapper.ApiLogMapper;
import com.ruoyi.system.service.IApiStatisticsService;

/**
 * API接口统计 服务层处理
 *
 * @author ruoyi
 */
@Service
public class ApiStatisticsServiceImpl implements IApiStatisticsService {
    /**
     * 每个接口保留的记录数
     */
    private static final int MAX_RECORDS_PER_API = 1000;
    private static final String[] SYSTEM_API_PREFIX =
            {"/system", "/login", "/monitor", "/getInfo", "/getRouters","/captchaImage"};

    @Autowired
    private ApiLogMapper mapper;

    /**
     * 保存API接口统计记录
     *
     * @param record API接口统计记录对象
     */
    @Override
    public void saveRecord(ApiLog record) {
        mapper.insertApiLog(record);
    }

    /**
     * 获取指定接口的统计信息
     *
     * @param apiKey 接口标识
     * @param limit
     * @return 统计信息
     */
    @Override
    public List<ApiLog> getLogList(String apiKey, Integer limit) {
        return mapper.selectApiLogList(apiKey,limit);
    }


    /**
     * 获取严重耗时接口列表（平均响应时长最长的接口）
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制
     * @return 严重耗时接口列表
     */
    @Override
    public List<ApiStatis> getSlowApiList(boolean includeSystem, int limit) {
        List<ApiStatis> apiStatis = mapper.selectStatis();
        return apiStatis.stream()
                .filter(item -> includeSystem ||
                        (Arrays.stream(SYSTEM_API_PREFIX).noneMatch(item.getApiKey()::contains)))
                .sorted((a, b) -> {
                    double avgA = a.getAvgResponseTime() != null ? a.getAvgResponseTime() : 0.0;
                    double avgB = b.getAvgResponseTime() != null ? b.getAvgResponseTime() : 0.0;
                    return Double.compare(avgB, avgA); // 降序排列
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取频繁调用接口列表（调用次数最多的接口）
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制
     * @param days          天数，例如7表示近7天，30表示近30天，null表示全部
     * @return 频繁调用接口列表
     */
    @Override
    public List<ApiStatis> getFrequentApiList(boolean includeSystem, int limit, Integer days) {
        List<ApiStatis> apiStatis;

        if (days != null && days > 0) {
            // 计算日期：当前时间往前推days天
            LocalDateTime dateTime = LocalDateTime.now().minusDays(days);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dateStr = dateTime.format(formatter);
            apiStatis = mapper.selectStatisAfter(dateStr);
        } else {
            apiStatis = mapper.selectStatis();
        }

        return apiStatis.stream()
                .filter(item -> includeSystem ||
                        (Arrays.stream(SYSTEM_API_PREFIX).noneMatch(item.getApiKey()::contains)))
                .sorted((a, b) -> {
                    long countA = a.getTotalCount() != null ? a.getTotalCount() : 0L;
                    long countB = b.getTotalCount() != null ? b.getTotalCount() : 0L;
                    return Long.compare(countB, countA); // 降序排列
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取异常接口列表（异常次数最多的接口）
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制
     * @return 异常接口列表
     */
    @Override
    public List<ApiStatis> getErrorApiList(boolean includeSystem, int limit) {
        List<ApiStatis> apiStatis = mapper.selectErrorApiList();
        return apiStatis.stream()
                .filter(item -> includeSystem ||
                        (Arrays.stream(SYSTEM_API_PREFIX).noneMatch(item.getApiKey()::contains)))
                .sorted((a, b) -> {
                    long errorA = a.getErrorCount() != null ? a.getErrorCount() : 0L;
                    long errorB = b.getErrorCount() != null ? b.getErrorCount() : 0L;
                    return Long.compare(errorB, errorA); // 降序排列
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 手动清理指定接口的旧记录
     *
     * @param apiKey 接口标识
     */
    @Override
    public void cleanOldRecords() {
        mapper.deleteOldLog(MAX_RECORDS_PER_API);
    }
    @Override
    public void cleanErrors() {
        mapper.cleanErrors();
    }


}

