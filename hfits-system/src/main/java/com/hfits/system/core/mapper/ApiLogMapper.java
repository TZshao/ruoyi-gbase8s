package com.hfits.system.core.mapper;

import java.util.List;

import com.hfits.system.core.domain.vo.ApiStatis;
import org.apache.ibatis.annotations.Param;
import com.hfits.system.core.domain.ApiLog;

/**
 * API接口统计记录 数据层
 *
 * @author hfits
 */
public interface ApiLogMapper {

    public void insertApiLog(ApiLog record);
    public int cleanErrors();
    public int deleteOldLog(@Param("keepCount") int keepCount);


    public List<ApiLog> selectApiLogList(@Param("apiKey") String apiKey, @Param("limit") Integer limit);

    public List<ApiStatis> selectStatis();

    public List<ApiStatis> selectStatisAfter(@Param("date") String date);

    public List<ApiStatis> selectErrorApiList();

}

