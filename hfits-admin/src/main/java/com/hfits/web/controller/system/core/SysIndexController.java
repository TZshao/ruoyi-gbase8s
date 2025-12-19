package com.hfits.web.controller.system.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hfits.common.config.HfitsConfig;
import com.hfits.common.utils.StringUtils;

/**
 * 首页
 *
 * @author hfits
 */
@RestController
public class SysIndexController
{
    /** 系统基础配置 */
    @Autowired
    private HfitsConfig hfitsConfig;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index()
    {
        return StringUtils.format("后台管理框架版本：v{}，请通过前端地址访问。", hfitsConfig.getVersion());
    }
}
