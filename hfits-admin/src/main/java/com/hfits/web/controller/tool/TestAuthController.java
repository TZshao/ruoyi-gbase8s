package com.hfits.web.controller.tool;

import com.hfits.common.core.controller.BaseController;
import com.hfits.common.core.domain.entity.SysUser;
import com.hfits.common.enums.FieldGroup;
import com.hfits.common.utils.auth.AuthUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/moduelTest/")
public class TestAuthController extends BaseController {

    @RequestMapping("/authSql")
    public String authSql(String dataScop, FieldGroup fieldGroup, String... deptCode) {
        SysUser user = new SysUser();
        user.setUserName("tester");
        user.setDataScope(dataScop);
        user.setDeptCode("userDeptCode");
        user.setAuthDeptCodes(deptCode);
        return AuthUtil.build(user, "t", fieldGroup);
    }
}
