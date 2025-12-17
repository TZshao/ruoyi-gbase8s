package com.ruoyi.framework.aspectj;

import com.ruoyi.common.enums.FieldGroup;
import com.ruoyi.common.utils.auth.AuthUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 数据过滤处理
 *
 * @author ruoyi
 */
@Aspect
@Component
public class DataScopeAspect {

    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";
    private static final Logger log = LoggerFactory.getLogger(DataScopeAspect.class);

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope) throws Throwable {
        clearDataScope(point);
        handleDataScope(point, controllerDataScope);
    }

    protected void handleDataScope(final JoinPoint joinPoint, DataScope controllerDataScope) {
        // 获取当前的用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNotNull(loginUser)) {
            SysUser currentUser = loginUser.getUser();
            // 如果是超级管理员，则不过滤数据
            if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin()) {
                // 获取参数对象
                Object[] params = joinPoint.getArgs();
                BaseEntity baseEntity = null;
                for (Object param : params) {
                    if (param instanceof BaseEntity) {
                        baseEntity = (BaseEntity) param;
                    }
                }

                if (StringUtils.isNotNull(baseEntity)) {
                    // 获取表别名和字段组
                    String tableAlias = controllerDataScope.tableAlias();

                    //个人权限，部门权限用的字段组不一样
                    FieldGroup fieldGroup = AuthUtil.DATA_SCOPE_SELF.equals(currentUser.getDataScope()) ?
                            controllerDataScope.selfFieldGroup() :
                            controllerDataScope.deptFieldGroup();

                    // 使用 AuthUtil 应用数据权限
                    AuthUtil.apply(baseEntity, currentUser, tableAlias, fieldGroup);
                }
            }
        }
    }


    /**
     * 拼接权限sql前先清空params.dataScope参数防止注入
     */
    private void clearDataScope(final JoinPoint joinPoint) {
        Object[] params = joinPoint.getArgs();
        BaseEntity baseEntity = null;
        for (Object param : params) {
            if (param instanceof BaseEntity) {
                baseEntity = (BaseEntity) param;
            }
        }
        if (StringUtils.isNotNull(baseEntity)) {
            baseEntity.getParams().put(DATA_SCOPE, "");
        }
    }
}
