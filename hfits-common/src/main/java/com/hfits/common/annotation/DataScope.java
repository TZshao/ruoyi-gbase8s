package com.hfits.common.annotation;

import com.hfits.common.enums.FieldGroup;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 *
 * @author hfits
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 表的别名
     */
    String tableAlias() default "";


//    同一个接口，拥有部门级权限的人，是通过 DeptCode之类的过滤，
//             但个人权限，是根据createUser之类的过滤，因此要配置两个

    /**
     * 部门权限字段组
     */
    FieldGroup deptFieldGroup();

    //个人权限字段组
    FieldGroup selfFieldGroup();
}
