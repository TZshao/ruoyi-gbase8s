package com.hfits.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FieldTypeUtil {

    /**
     * 值类型转换
     */
    public static Object convertValue(Object value, Class<?> targetType) throws Exception {
        if (value == null) {
            return null;
        }

        String strValue = value.toString().trim();
        if (StringUtils.isEmpty(strValue)) {
            return null;
        }

        // String类型
        if (targetType == String.class) {
            return strValue;
        }

        // Long类型
        if (targetType == Long.class || targetType == long.class) {
            try {
                return Long.parseLong(strValue);
            } catch (NumberFormatException e) {
                // 尝试解析为double再转long
                return (long) Double.parseDouble(strValue);
            }
        }

        // Integer类型
        if (targetType == Integer.class || targetType == int.class) {
            try {
                return Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                return (int) Double.parseDouble(strValue);
            }
        }

        // Double类型
        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(strValue);
        }

        // Float类型
        if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(strValue);
        }

        // Boolean类型
        if (targetType == Boolean.class || targetType == boolean.class) {
            String lower = strValue.toLowerCase();
            return "true".equals(lower) || "1".equals(lower) || "yes".equals(lower) || "y".equals(lower);
        }

        // Date类型
        if (targetType == Date.class || targetType == java.sql.Date.class) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(strValue);
            } catch (Exception e) {
                // 尝试其他格式
                try {
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.parse(strValue);
                } catch (Exception e2) {
                    throw new Exception("日期格式转换失败: " + strValue, e2);
                }
            }
        }

        // 其他类型，尝试直接转换
        return value;
    }

}
