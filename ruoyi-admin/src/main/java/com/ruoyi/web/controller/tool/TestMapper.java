package com.ruoyi.web.controller.tool;

import com.ruoyi.common.interfaces.LogMapper;
import org.springframework.stereotype.Component;

@Component
public class TestMapper implements LogMapper {
    @Override
    public Object logById(Object id) {
        UserEntity user = new UserEntity();
        user.setUsername("测试结果::"+id);
        System.out.println(user);
        return user;
    }
}
