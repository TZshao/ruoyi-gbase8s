package com.hfits.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.hfits.common.core.controller.BaseController;
import com.hfits.common.core.domain.AjaxResult;
import com.hfits.common.core.domain.model.RegisterBody;
import com.hfits.common.utils.StringUtils;
import com.hfits.framework.web.service.SysRegisterService;
import com.hfits.system.core.service.ISysConfigService;

/**
 * 注册验证
 *
 * @author hfits
 */
@RestController
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}
