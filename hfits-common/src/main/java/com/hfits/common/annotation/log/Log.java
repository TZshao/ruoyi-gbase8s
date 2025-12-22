package com.hfits.common.annotation.log;

import com.hfits.common.enums.BusinessType;
import com.hfits.common.enums.OperatorType;
import com.hfits.common.interfaces.LogHandler;
import com.hfits.common.interfaces.LogMapper;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 * 用法参见：
 *
 * @see com.hfits.web.controller.tool.TestController
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块
     */
    String title() default "";

    /**
     * 功能
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别
     */
    OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default true;

    /**
     * 排除指定的请求参数
     */
    String[] excludeParamNames() default {};

    // 默认DefaultHandler 根据 tableInfo[1] 拿到请求参数代表Id的值，调用mapper.ById 记录操作前后的值
    Class<? extends LogHandler> logHandler() default LogHandler.class;

    //没传则不会记录值变化
    Class<? extends LogMapper> mapper() default LogMapper.class;

    String[] tableInfo() default {"id"};

}
