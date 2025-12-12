package com.ruoyi.common.annotation;

import com.ruoyi.common.enums.FieldGroup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限过滤注解
 *
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 表的别名
     */
    public String tableAlias() default "";

    /**
     * 权限字符（用于多个角色匹配符合要求的权限）默认根据权限注解@ss获取，多个权限用逗号分隔开来
     */
    public String permission() default "";

    /**
     * 字段组（用于指定数据权限过滤的字段组，对应 FieldGroup 枚举名称）
     * 可选值：COMMON_CASE, CASE1
     */
    public FieldGroup fieldGroup();
}
