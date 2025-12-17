package com.ruoyi.common.core.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;

import com.ruoyi.common.core.domain.Resp;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.sql.SqlUtil;

/**
 * web层通用数据处理
 *
 * @author ruoyi
 */
public class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage() {
        PageUtils.startPage();
    }

    /**
     * 设置请求排序数据
     */
    protected void startOrderBy() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy())) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * 清理分页的线程变量
     */
    protected void clearPage() {
        PageUtils.clearPage();
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 返回成功
     */
    protected <T> Resp<T> successR() {
        return Resp.ok();
    }

    /**
     * 返回成功消息
     */
    protected <T> Resp<T> successR(String message) {
        return Resp.ok(null, message);
    }

    /**
     * 返回成功数据
     */
    protected <T> Resp<T> successR(T data) {
        return Resp.ok(data);
    }

    /**
     * 返回成功数据和消息
     */
    protected <T> Resp<T> successR(T data, String message) {
        return Resp.ok(data, message);
    }

    /**
     * 返回失败
     */
    protected <T> Resp<T> errorR() {
        return Resp.fail();
    }

    /**
     * 返回失败消息
     */
    protected <T> Resp<T> errorR(String message) {
        return Resp.fail(message);
    }

    /**
     * 返回失败数据和消息
     */
    protected <T> Resp<T> errorR(T data, String message) {
        return Resp.fail(data, message);
    }

    /**
     * 返回失败，带状态码和消息
     */
    protected <T> Resp<T> errorR(int code, String message) {
        return Resp.fail(code, message);
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected <T> Resp<T> toAjaxR(int rows) {
        return rows > 0 ? Resp.ok() : Resp.fail();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected <T> Resp<T> toAjaxR(boolean result) {
        return result ? Resp.ok() : Resp.fail();
    }

    /**
     * 返回成功
     */
    @Deprecated
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     */
    @Deprecated
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功消息
     * @deprecated 请使用 {@link #successR(String)} 方法
     */
    @Deprecated
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * 返回成功消息
     * @deprecated 请使用 {@link #successR(Object)} 方法
     */
    @Deprecated
    public AjaxResult success(Object data) {
        return AjaxResult.success(data);
    }

    /**
     * 返回失败消息
     * @deprecated 请使用 {@link #errorR(String)} 方法
     */
    @Deprecated
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    /**
     * 返回警告消息
     * @deprecated 请使用 {@link #warnR(String)} 方法
     */
    @Deprecated
    public AjaxResult warn(String message) {
        return AjaxResult.warn(message);
    }

    /**
     * 响应返回结果
     *
     * @deprecated 请使用 {@link #toAjaxR(int)} 方法
     * @param rows 影响行数
     * @return 操作结果
     */
    @Deprecated
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 响应返回结果
     *
     * @deprecated 请使用 {@link #toAjaxR(boolean)} 方法
     * @param result 结果
     * @return 操作结果
     */
    @Deprecated
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StringUtils.format("redirect:{}", url);
    }

    /**
     * 获取用户缓存信息
     */
    public LoginUser getLoginUser() {
        return SecurityUtils.getLoginUser();
    }
    /**
     * 获取用户缓存信息
     */
    public SysUser getUser() {
        return SecurityUtils.getLoginUser().getUser();
    }

    /**
     * 获取登录用户id
     */
    public Long getUserId() {
        return getLoginUser().getUserId();
    }

    /**
     * 获取登录部门id
     */
    public Long getDeptId() {
        return getLoginUser().getDeptId();
    }

    /**
     * 获取登录用户名
     */
    public String getUsername() {
        return getLoginUser().getUsername();
    }
}
