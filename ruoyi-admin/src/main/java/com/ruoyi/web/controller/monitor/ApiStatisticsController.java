package com.ruoyi.web.controller.monitor;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.Resp;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.ApiLog;
import com.ruoyi.system.domain.vo.ApiStatis;
import com.ruoyi.system.service.IApiStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.*;

/**
 * API接口统计
 * 虽然 druid也有统计功能，但一个是不能持久化，另一个只能看到平均时长
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/api/statistics")
public class ApiStatisticsController extends BaseController {
    @Autowired
    private IApiStatisticsService apiStatisticsService;

    /**
     * 获取严重耗时接口列表
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制，默认10
     */
    @PreAuthorize("@ss.hasPermi('monitor:apiStatistics:list')")
    @GetMapping("/slow")
    public Resp<List<ApiStatis>> getSlowApiList(Boolean includeSystem, Integer limit) {
        boolean include = includeSystem != null && includeSystem;
        int limitCount = limit != null && limit > 0 ? limit : 10;
        List<ApiStatis> list = apiStatisticsService.getSlowApiList(include, limitCount);
        return successR(list);
    }

    /**
     * 获取频繁调用接口列表（近N天调用次数）
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制，默认10
     * @param days          天数，例如7表示近7天，30表示近30天，null表示全部
     */
    @PreAuthorize("@ss.hasPermi('monitor:apiStatistics:list')")
    @GetMapping("/frequent")
    public Resp<List<ApiStatis>> getFrequentApiList(Boolean includeSystem, Integer limit, Integer days) {
        boolean include = includeSystem != null && includeSystem;
        int limitCount = limit != null && limit > 0 ? limit : 10;
        List<ApiStatis> list = apiStatisticsService.getFrequentApiList(include, limitCount, days);
        return successR(list);
    }

    /**
     * 获取异常接口列表
     *
     * @param includeSystem 是否包含/system开头的接口
     * @param limit         返回数量限制，默认10
     */
    @PreAuthorize("@ss.hasPermi('monitor:apiStatistics:list')")
    @GetMapping("/error")
    public Resp<List<ApiStatis>> getErrorApiList(Boolean includeSystem, Integer limit) {
        boolean include = includeSystem != null && includeSystem;
        int limitCount = limit != null && limit > 0 ? limit : 10;
        List<ApiStatis> list = apiStatisticsService.getErrorApiList(include, limitCount);
        return successR(list);
    }

    /**
     * 获取指定接口的统计（List只有耗时和调用时间）
     *
     * @param apiKey 接口标识
     */
    @PreAuthorize("@ss.hasPermi('monitor:apiStatistics:query')")
    @GetMapping("/detail")
    public Resp<List<Map<String, Object>>> getInfo(String apiKey,Integer limit) {
//        if (StringUtils.isEmpty(apiKey)) {
//            return errorR("接口标识不能为空");
//        }
//        List<ApiLog> list = apiStatisticsService.getLogList(apiKey,limit);
//        List<Map<String, Object>> resultMap = new ArrayList<>();
//        //忽略无用字段减少数据量
//        list.forEach(item ->
//                resultMap.add(Map.of("create_time", item.getCreateTime(), "responseTime", item.getResponseTime())));
//        return successR(resultMap);

        List<Map<String, Object>> resultMap = new ArrayList<>();
        long now = System.currentTimeMillis();
        Random random = new Random();

        for (int i = 0; i < limit; i++) {
            Map<String, Object> map = new HashMap<>();
            // 假定每条数据的create_time递减，模拟最近2k次调用
            map.put("create_time", new Date(now - i * 1000L * 60)); // 间隔1分钟
            map.put("responseTime", 50 + random.nextInt(950)); // 50ms~999ms
            resultMap.add(map);
        }

        return successR(resultMap);
    }

    /**
     * 手动清理指定接口的旧记录（保留最新1000条）
     *
     * @param apiKey 接口标识
     */
    @PreAuthorize("@ss.hasPermi('monitor:apiStatistics:remove')")
    @DeleteMapping("/clean")
    public Resp<Void> cleanOldRecords() {
        apiStatisticsService.cleanOldRecords();
        return successR();
    }

    @PreAuthorize("@ss.hasPermi('monitor:apiStatistics:remove')")
    @DeleteMapping("/deleteErrors")
    public Resp<Void> cleanErrors() {
        apiStatisticsService.cleanErrors();
        return successR();
    }


}

