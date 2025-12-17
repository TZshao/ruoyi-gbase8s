package com.ruoyi.system.service;

import java.util.List;

import com.ruoyi.system.domain.ApiLog;
import com.ruoyi.system.domain.vo.ApiStatis;


/**
 * API接口统计 服务层
 *
 * @author ruoyi
 */
public interface IApiStatisticsService {
    /**
     * 保存API接口统计记录
     *
     * @param record API接口统计记录对象
     */
    public void saveRecord(ApiLog record);

    /**
     * 获取指定接口的统计信息
     *
     * @param apiKey 接口标识
     * @param limit
     * @return 统计信息
     */
    public List<ApiLog> getLogList(String apiKey, Integer limit);


    /**
     * 获取严重耗时接口列表（平均响应时长最长的接口）
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制
     * @return 严重耗时接口列表
     */
    public List<ApiStatis> getSlowApiList(boolean includeSystem, int limit);

    /**
     * 获取频繁调用接口列表（调用次数最多的接口）
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制
     * @param days          天数，例如7表示近7天，30表示近30天，null表示全部
     * @return 频繁调用接口列表
     */
    public List<ApiStatis> getFrequentApiList(boolean includeSystem, int limit, Integer days);

    /**
     * 获取异常接口列表（异常次数最多的接口）
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制
     * @return 异常接口列表
     */
    public List<ApiStatis> getErrorApiList(boolean includeSystem, int limit);

    /**
     * 手动清理指定接口的旧记录
     *
     * @param apiKey 接口标识
     */
    public void cleanOldRecords();

    void cleanErrors();
}

