package com.ruoyi.framework.aspectj;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.interfaces.LogHandler;
import com.ruoyi.framework.aspectj.handler.DefaultLogHandler;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.log.Log;
import com.ruoyi.common.annotation.log.Module;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessStatus;
import com.ruoyi.common.enums.HttpMethod;
import com.ruoyi.common.utils.ExceptionUtil;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.domain.SysOperLog;

/**
 * 操作日志记录处理
 *
 * @author ruoyi
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private LogHandler[] handlers;
    private final Map<Class<?>, LogHandler> handlerMap = new ConcurrentHashMap<Class<?>, LogHandler>();

    @PostConstruct
    public void init() {
        for (LogHandler handler : handlers) {
            handlerMap.put(handler.getClass(), handler);
        }
        handlerMap.put(LogHandler.class, handlerMap.get(DefaultLogHandler.class));
    }

    /**
     * 计算操作消耗时间
     */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<Long>("Cost Time");
    private static final ThreadLocal<JSONObject> BEFORE_THREADLOCAL = new NamedThreadLocal<JSONObject>("beforeEntity");

    /**
     * 参数最大长度限制
     */
    private static final int PARAM_MAX_LENGTH = 2000;

    /**
     * 处理请求前执行
     * 支持复合注解（如 @PostMappingLog、@DeleteMappingLog、@PutMappingLog 等）
     */
    @Before(value = "@annotation(com.ruoyi.common.annotation.log.Log) || " +
                     "@annotation(com.ruoyi.common.annotation.log.PostMappingLog) || " +
                     "@annotation(com.ruoyi.common.annotation.log.DeleteMappingLog) || " +
                     "@annotation(com.ruoyi.common.annotation.log.PutMappingLog)")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Log controllerLog = AnnotationUtils.findAnnotation(signature.getMethod(), Log.class);
        if (controllerLog == null) {
            return;
        }
        LogHandler handler = getHandler(controllerLog.logHandler());
        //这里signature.getParameterNames依赖于编译参数 -parameters 保留方法原始参数名称，避免 arg0，arg1的情况
        BEFORE_THREADLOCAL.set(handler.recordPreValue(controllerLog, joinPoint.getArgs(),signature.getParameterNames()));
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /**
     * 处理完请求后执行
     * 支持复合注解（如 @PostMappingLog、@DeleteMappingLog、@PutMappingLog 等）
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(com.ruoyi.common.annotation.log.Log) || " +
                                "@annotation(com.ruoyi.common.annotation.log.PostMappingLog) || " +
                                "@annotation(com.ruoyi.common.annotation.log.DeleteMappingLog) || " +
                                "@annotation(com.ruoyi.common.annotation.log.PutMappingLog)",
                    returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Log controllerLog = AnnotationUtils.findAnnotation(signature.getMethod(), Log.class);
        if (controllerLog != null) {
            handleLog(joinPoint, controllerLog, null, jsonResult);
        }
    }

    /**
     * 拦截异常操作
     * 支持复合注解（如 @PostMappingLog、@DeleteMappingLog、@PutMappingLog 等）
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(com.ruoyi.common.annotation.log.Log) || " +
                            "@annotation(com.ruoyi.common.annotation.log.PostMappingLog) || " +
                            "@annotation(com.ruoyi.common.annotation.log.DeleteMappingLog) || " +
                            "@annotation(com.ruoyi.common.annotation.log.PutMappingLog)",
                    throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Log controllerLog = AnnotationUtils.findAnnotation(signature.getMethod(), Log.class);
        if (controllerLog != null) {
            handleLog(joinPoint, controllerLog, e, null);
        }
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 获取当前的用户
//           LoginUser loginUser = SecurityUtils.getLoginUser();
             LoginUser loginUser = null;
            LogHandler handler = getHandler(controllerLog.logHandler());

            // *========数据库日志=========*//
            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            String ip = IpUtils.getIpAddr();
            operLog.setOperIp(ip);
            operLog.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            if (loginUser != null) {
                operLog.setOperName(loginUser.getUsername());
                SysUser currentUser = loginUser.getUser();
                if (StringUtils.isNotNull(currentUser) && StringUtils.isNotNull(currentUser.getDept())) {
                    operLog.setDeptName(currentUser.getDept().getDeptName());
                }
            }

            if (e != null) {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(Convert.toStr(e.getMessage(), ExceptionUtil.getExceptionMessage(e)), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            operLog.setMethod(className + "." + methodName + "()");
            // 设置模块名称，从类上的@Module注解获取，没有则默认'其他'
            Class<?> targetClass = joinPoint.getTarget().getClass();
            Module moduleAnnotation = targetClass.getAnnotation(Module.class);
            String moduleName = (moduleAnnotation != null) ? moduleAnnotation.value() : "其他";
            operLog.setModule(moduleName);
            //设置操作前后的值变化
            operLog.setBeforeOpt(BEFORE_THREADLOCAL.get().toJSONString());
            operLog.setAfterOpt(handler.recodeAfterValue(controllerLog, joinPoint.getArgs(), signature.getParameterNames()).toJSONString());
            // 设置请求方式
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 设置消耗时间
            operLog.setCostTime(System.currentTimeMillis() - TIME_THREADLOCAL.get());
            // 保存数据库
            AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("异常信息:{}", exp.getMessage());
        } finally {
            TIME_THREADLOCAL.remove();
            BEFORE_THREADLOCAL.remove();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log     日志
     * @param operLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult) throws Exception {
        // 设置action动作
        operLog.setBusinessType(log.businessType().ordinal());
        // 设置标题
        operLog.setTitle(log.title());
        // 设置操作人类别
        operLog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operLog, log.excludeParamNames());
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult)) {
            operLog.setJsonResult(StringUtils.substring(JSON.toJSONString(jsonResult), 0, 2000));
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog, String[] excludeParamNames) throws Exception {
        String requestMethod = operLog.getRequestMethod();
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        if (StringUtils.isEmpty(paramsMap) && StringUtils.equalsAny(requestMethod, HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name())) {
            String params = argsArrayToString(joinPoint.getArgs());
            operLog.setOperParam(params);
        } else {
            operLog.setOperParam(StringUtils.substring(JSON.toJSONString(paramsMap), 0, PARAM_MAX_LENGTH));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (StringUtils.isNotNull(o) && !isFilterObject(o)) {
                    try {
                        String jsonObj = JSON.toJSONString(o);
                        params.append(jsonObj).append(" ");
                        if (params.length() >= PARAM_MAX_LENGTH) {
                            return StringUtils.substring(params.toString(), 0, PARAM_MAX_LENGTH);
                        }
                    } catch (Exception e) {
                        log.error("请求参数拼装异常 msg:{}, 参数:{}", e.getMessage(), paramsArray, e);
                    }
                }
            }
        }
        return params.toString();
    }


    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }

    public LogHandler getHandler(Class<?> clazz) {
        LogHandler handler = handlerMap.get(clazz);
        if (handler != null) {
            return handler;
        }
        throw new RuntimeException("找不到日志处理器");
    }
}
