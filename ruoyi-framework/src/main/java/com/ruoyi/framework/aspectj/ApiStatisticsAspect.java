package com.ruoyi.framework.aspectj;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.domain.ApiLog;
import jakarta.servlet.http.HttpServletRequest;

/**
 * API接口统计切面
 *
 * @author ruoyi
 */
@Aspect
@Component
@Order(2)
public class ApiStatisticsAspect
{
    private static final Logger log = LoggerFactory.getLogger(ApiStatisticsAspect.class);

    /**
     * 配置切点：拦截所有RestController的方法
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) && execution(public * *(..))")
    public void apiPointCut()
    {
    }

    /**
     * 环绕通知：统计接口调用次数和响应时长
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("apiPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable
    {
        long startTime = System.currentTimeMillis();
        Object result = null;
        String status = "0"; // 0正常 1异常
        String errorMsg = null;

        try
        {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        }
        catch (Throwable e)
        {
            status = "1";
            errorMsg = e.getMessage();
            if (StringUtils.isEmpty(errorMsg))
            {
                errorMsg = e.getClass().getName();
            }
            // 异常信息过长时截取
            if (errorMsg != null && errorMsg.length() > 2000)
            {
                errorMsg = errorMsg.substring(0, 2000);
            }
            throw e;
        }
        finally
        {
            try
            {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                // 获取请求信息
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null)
                {
                    HttpServletRequest request = attributes.getRequest();
                    String httpMethod = request.getMethod();
                    String requestUri = request.getRequestURI();
                    String apiKey = requestUri+"@"+httpMethod;

                    // 构建统计记录对象
                    ApiLog record = new ApiLog();
                    record.setApiKey(apiKey);
                    record.setHttpMethod(httpMethod);
                    record.setRequestUri(requestUri);
                    record.setResponseTime(duration);
                    record.setStatus(status);
                    record.setErrorMsg(errorMsg);

                    // 异步保存统计记录
                    AsyncManager.me().execute(AsyncFactory.recordApiStatistics(record));
                }
            }
            catch (Exception e)
            {
                // 记录统计异常，不影响主流程
                log.error("API统计记录异常:{}", e.getMessage());
            }
        }
    }
}

