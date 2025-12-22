package com.hfits.framework.aspectj.handler;

import com.alibaba.fastjson2.JSONObject;
import com.hfits.common.annotation.log.Log;
import com.hfits.common.enums.BusinessType;
import com.hfits.common.interfaces.LogHandler;
import com.hfits.common.interfaces.LogMapper;
import com.hfits.common.utils.reflect.ReflectUtils;
import com.hfits.common.utils.spring.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认日志处理器
 * 优化点：
 * 2. 提取公共方法消除代码重复
 * 3. 优化JSON转换逻辑
 * 4. 改进异常处理和边界检查
 * 5. 优化字符串处理
 */
@Component
public class DefaultLogHandler implements LogHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultLogHandler.class);

    /**
     * LogMapper缓存，避免每次从Spring容器查找
     * 使用ConcurrentHashMap保证线程安全
     */
    private static final Map<Class<? extends LogMapper>, LogMapper> MAPPER_CACHE = new ConcurrentHashMap<>();

    /**
     * tableInfo数组索引常量
     */
    private static final int ID_INDEX = 0;

    @Override
    public JSONObject recordPreValue(Log log, Object[] args, String[] parameterNames) {
        JSONObject result = new JSONObject();
        if (log == null || log.mapper().equals(LogMapper.class)) {
            return result;
        }

        return getEntityValue(log, args, parameterNames, "操作前");
    }

    @Override
    public JSONObject recodeAfterValue(Log log, Object[] args, String[] parameterNames) {
        JSONObject result = new JSONObject();
        if (log == null || log.mapper().equals(LogMapper.class)
                || log.businessType().equals(BusinessType.DELETE)) {
            return result;
        }

        return getEntityValue(log, args, parameterNames, "操作后");
    }

    /**
     * 获取实体值的公共方法
     * 提取公共逻辑，消除代码重复
     *
     * @param log            日志注解
     * @param args           方法参数值数组
     * @param parameterNames 方法参数名数组
     * @param operationType  操作类型（用于日志）
     * @return 实体值的JSONObject
     */
    private JSONObject getEntityValue(Log log, Object[] args, String[] parameterNames, String operationType) {
        JSONObject result = new JSONObject();

        try {
            // 检查tableInfo数组
            String[] tableInfo = log.tableInfo();
            String idName = tableInfo[ID_INDEX];
            if (StringUtils.isBlank(idName)) {
                return result;
            }

            // 获取Mapper（使用缓存）
            LogMapper mapper = getMapper(log.mapper());
            if (mapper == null) {
                logger.warn("无法获取LogMapper: {}", log.mapper().getName());
                return result;
            }

            // 获取ID值
            Object idValue = getIdValue(idName, args, parameterNames);
            if (idValue == null) {
                logger.debug("未找到ID值: {}", idName);
                return result;
            }

            // 调用mapper获取实体值
            Object entityValue = mapper.logById(idValue);
            if (entityValue != null) {
                result = convertToJSONObject(entityValue);
            }

        } catch (IllegalArgumentException | NullPointerException e) {
            logger.warn("参数错误，跳过{}日志记录: {}", operationType, e.getMessage());
        } catch (Exception e) {
            logger.error("{}日志记录异常", operationType, e);
        }

        return result;
    }

    /**
     * 获取LogMapper，使用缓存优化性能
     *
     * @param mapperClass Mapper类
     * @return LogMapper实例
     */
    private LogMapper getMapper(Class<? extends LogMapper> mapperClass) {
        return MAPPER_CACHE.computeIfAbsent(mapperClass, clazz -> {
            try {
                return SpringUtils.getBean(clazz);
            } catch (Exception e) {
                logger.error("获取LogMapper失败: {}", clazz.getName(), e);
                return null;
            }
        });
    }

    /**
     * 根据idName从参数中获取ID值
     * 支持简单参数名（如：id）和嵌套属性（如：paramDto.id）
     *
     * @param idName         ID参数名，可以是简单参数名或嵌套属性路径
     * @param args           方法参数值数组
     * @param parameterNames 方法参数名数组
     * @return ID值，如果未找到返回null
     */
    private Object getIdValue(String idName, Object[] args, String[] parameterNames) {
        if (StringUtils.isBlank(idName) || args == null || parameterNames == null) {
            return null;
        }

        // 检查是否为嵌套属性（包含.）
        int dotIndex = idName.indexOf('.');
        if (dotIndex > 0) {
            // 嵌套属性，如 paramDto.id
            String paramName = idName.substring(0, dotIndex);
            String propertyPath = idName.substring(dotIndex + 1);

            // 在parameterNames中查找参数位置
            int paramIndex = findParameterIndex(paramName, parameterNames);

            if (paramIndex >= 0 && paramIndex < args.length && args[paramIndex] != null) {
                // 找到参数对象，通过反射获取嵌套属性值
                try {
                    return ReflectUtils.invokeGetter(args[paramIndex], propertyPath);
                } catch (Exception e) {
                    logger.warn("获取嵌套属性值失败: {}.{}", paramName, propertyPath, e);
                    return null;
                }
            }
        } else {
            // 简单参数名，直接在parameterNames中查找
            int paramIndex = findParameterIndex(idName, parameterNames);
            if (paramIndex >= 0 && paramIndex < args.length) {
                return args[paramIndex];
            }
        }

        return null;
    }

    /**
     * 查找参数在parameterNames中的索引位置
     *
     * @param paramName      参数名
     * @param parameterNames 参数名数组
     * @return 索引位置，未找到返回-1
     */
    private int findParameterIndex(String paramName, String[] parameterNames) {
        if (paramName == null || parameterNames == null) {
            return -1;
        }

        for (int i = 0; i < parameterNames.length; i++) {
            if (paramName.equals(parameterNames[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 将对象转换为JSONObject
     * 优化：根据对象类型选择最合适的转换方式
     *
     * @param obj 待转换的对象
     * @return JSONObject
     */
    private JSONObject convertToJSONObject(Object obj) {
        if (obj == null) {
            return new JSONObject();
        }

        // 如果已经是JSONObject，直接返回
        if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        }

        // 如果是Map，直接构造
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            return new JSONObject(map);
        }

        // 其他类型，使用JSON序列化（慢，通用）
        try {
            return JSONObject.parseObject(JSONObject.toJSONString(obj));
        } catch (Exception e) {
            logger.warn("对象转换为JSONObject失败: {}", obj.getClass().getName(), e);
            return new JSONObject();
        }
    }
}

