package com.hfits.system.core.mapper;

import com.hfits.system.core.domain.ApiLog;
import com.hfits.system.core.domain.vo.ApiStatis;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * API接口统计记录 数据层
 *
 * @author hfits
 */
public interface ApiLogMapper {

    void insertApiLog(ApiLog record);

    int cleanErrors();

    int deleteOldLog(@Param("keepCount") int keepCount);


    List<ApiLog> selectApiLogList(@Param("apiKey") String apiKey, @Param("limit") Integer limit);

    List<ApiStatis> selectStatis();

    List<ApiStatis> selectStatisAfter(@Param("date") String date);

    List<ApiStatis> selectErrorApiList();

}

