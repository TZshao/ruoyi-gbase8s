package com.ruoyi.common.annotation.log;

import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.enums.OperatorType;
import com.ruoyi.common.interfaces.LogHandler;
import com.ruoyi.common.interfaces.LogMapper;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.*;

/**
 * 复合注解：组合 @DeleteMapping 和 @Log
 * 默认 businessType = BusinessType.DELETE
 *
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.DELETE)
@Log
public @interface DeleteMappingLog {
    /**
     * 映射到 @Log 的 title 属性
     */
    @AliasFor(annotation = Log.class, attribute = "title")
    String title();

    /**
     * 映射到 @Log 的 operatorType 属性
     */
    @AliasFor(annotation = Log.class, attribute = "operatorType")
    OperatorType operatorType() default OperatorType.MANAGE;

    @AliasFor(annotation = Log.class, attribute = "businessType")
    BusinessType businessType() default BusinessType.DELETE;

    /**
     * 映射到 @Log 的 isSaveRequestData 属性
     */
    @AliasFor(annotation = Log.class, attribute = "isSaveRequestData")
    boolean isSaveRequestData() default true;

    /**
     * 映射到 @Log 的 isSaveResponseData 属性
     */
    @AliasFor(annotation = Log.class, attribute = "isSaveResponseData")
    boolean isSaveResponseData() default true;

    /**
     * 映射到 @Log 的 excludeParamNames 属性
     */
    @AliasFor(annotation = Log.class, attribute = "excludeParamNames")
    String[] excludeParamNames() default {};

    /**
     * 映射到 @Log 的 handler 属性
     */
    @AliasFor(annotation = Log.class, attribute = "logHandler")
    Class<? extends LogHandler> logHandler() default LogHandler.class;

    /**
     * 映射到 @Log 的 mapper 属性
     */
    @AliasFor(annotation = Log.class, attribute = "mapper")
    Class<? extends LogMapper> mapper() default LogMapper.class;

    /**
     * 映射到 @Log 的 tableInfo 属性
     */
    @AliasFor(annotation = Log.class, attribute = "tableInfo")
    String[] tableInfo() default {"id"};


    /**
     * 映射到 @RequestMapping 的 value 属性
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};

    /**
     * 映射到 @RequestMapping 的 name 属性
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "name")
    String name() default "";

    /**
     * 映射到 @RequestMapping 的 path 属性
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] path() default {};

    /**
     * 映射到 @RequestMapping 的 params 属性
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "params")
    String[] params() default {};

    /**
     * 映射到 @RequestMapping 的 headers 属性
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "headers")
    String[] headers() default {};

    /**
     * 映射到 @RequestMapping 的 consumes 属性
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "consumes")
    String[] consumes() default {};

    /**
     * 映射到 @RequestMapping 的 produces 属性
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "produces")
    String[] produces() default {};


}

