package com.hfits.quartz.task;

import org.springframework.stereotype.Component;

/**
 * 定时任务调度测试
 *
 * @author hfits
 */
@Component("ryTask")
public class RyTask {
    public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i) {
    }

    public void ryParams(String params) {
    }

    public void ryNoParams() {
    }
}
