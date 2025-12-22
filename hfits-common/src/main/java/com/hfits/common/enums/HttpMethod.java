package com.hfits.common.enums;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求方式
 *
 * @author hfits
 */
public enum HttpMethod {
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

    private static final Map<String, HttpMethod> METHOD_MAP = new HashMap<>(16);

    static {
        for (HttpMethod httpMethod : values()) {
            METHOD_MAP.put(httpMethod.name(), httpMethod);
        }
    }

    @Nullable
    public static HttpMethod resolve(@Nullable String method) {
        return (method != null ? METHOD_MAP.get(method) : null);
    }

    public boolean matches(String method) {
        return (this == resolve(method));
    }
}
