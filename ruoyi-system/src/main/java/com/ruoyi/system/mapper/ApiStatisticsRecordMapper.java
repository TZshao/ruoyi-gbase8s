package com.ruoyi.system.mapper;

import java.util.List;

import com.ruoyi.system.domain.vo.ApiStatis;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.ApiLog;

/**
 * API接口统计记录 数据层
 *
 * @author ruoyi
 */
public interface ApiStatisticsRecordMapper {
    /**
     * 新增API接口统计记录
     *
     * @param record API接口统计记录对象
     */
    public void insertApiStatisticsRecord(ApiLog record);


    /**
     * 统计指定接口的调用总数
     *
     * @param apiKey 接口标识
     * @return 调用总数
     */
    public int countByApiKey(String apiKey);


    public List<ApiLog> selectApiRecordList(String apiKey);

    /**
     * 删除超过保留数量的旧记录
     *
     * @param apiKey    接口标识
     * @param keepCount 保留数量
     * @return 删除的记录数
     */
    public int deleteOldRecords(@Param("keepCount") int keepCount);


    /**
     * 查询所有接口的统计汇总（分组）
     *
     * @return 统计汇总信息列表
     */
    public List<ApiStatis> selectAllStatisticsSummary();

    /**
     * 查询异常接口列表（按接口分组统计异常次数）
     *
     * @return 异常接口统计列表
     */
    public List<ApiStatis> selectErrorApiList();
}

