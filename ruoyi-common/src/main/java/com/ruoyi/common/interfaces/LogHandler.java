package com.ruoyi.common.interfaces;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.log.Log;

public interface LogHandler {

    //不记录时返回空json，不要返回null ！
    JSONObject recordPreValue(Log controllerLog, Object[] args, String[] parameterNames);

    //不记录时返回空json，不要返回null ！
    JSONObject recodeAfterValue(Log controllerLog, Object[] args, String[] parameterNames);
}
