package com.ruoyi.web.controller.tool;

import com.ruoyi.common.annotation.log.Log;
import com.ruoyi.common.annotation.log.Module;
import com.ruoyi.common.annotation.log.PostMappingLog;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.framework.aspectj.handler.DefaultLogHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.*;

@Module("日志注解示例")
@RestController
@RequestMapping("/test/user")
public class TestController extends BaseController {

    /**
     * handler获取mapper的bean，将 tableInfo的值视为ID
     * 调用mapper的log方法获取操作前后的值
     * handler可自定义，空置时默认为DefaultLogHandler
     */

    @GetMapping("/{userId}")
    @Log(title = "测试",
            businessType = BusinessType.OTHER,
            mapper = TestMapper.class,
            tableInfo = {"userId"})
    public void getUser(@PathVariable(name = "userId") Integer userId) {
    }

//    @PostMappingLog 等效于 @Log+@PostMapping,businessType 默认 Update
    @PostMappingLog(
            value = "/update",
            title = "用户更新",
            logHandler = DefaultLogHandler.class,
            mapper = TestMapper.class,
            tableInfo = {"user.Id"}) //先找user的参数，再找user的Id的属性，还可以更深
    public void update(@RequestBody UserEntity user) {}

}





class UserEntity {
    private Integer userId;
    private String username;
    private String password;
    private String mobile;

    public UserEntity() {

    }

    public UserEntity(Integer userId, String username, String password, String mobile) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.mobile = mobile;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", mobile='" + mobile + '\'' +
                '}';
    }
}
