package com.hfits.system.workflow.service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfits.system.workflow.domain.FlowInstance;
import com.hfits.system.workflow.domain.FlowStepDef;
import com.hfits.system.workflow.trigger.FlowTrigger;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.mapper.FlowStepDefMapper;

import jakarta.annotation.PostConstruct;

/**
 * 流程步骤定义Service业务层处理
 */
@Service
public class FlowStepDefService {
    @Autowired
    private FlowStepDefMapper flowStepDefMapper;

    @Autowired
    private FlowTrigger[] flowTrigger;

    private final Map<String, List<String>> tiggerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (FlowTrigger flowTrigger : flowTrigger) {
            Class<?> clazz = flowTrigger.getClass();
            List<String> methodList = tiggerMap.computeIfAbsent(clazz.getSimpleName(), k -> new ArrayList<>());
            for (Method method : clazz.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && FlowInstance.class.isAssignableFrom(params[0])) {
                    methodList.add(method.getName());
                }
            }
            if (methodList.isEmpty()) {
                tiggerMap.remove(clazz.getSimpleName());
            }
        }
    }

    public FlowStepDef selectFlowStepDefById(Long id) {
        return flowStepDefMapper.selectFlowStepDefById(id);
    }


    public List<FlowStepDef> selectFlowStepDefList(FlowStepDef flowStepDef) {
        return flowStepDefMapper.selectFlowStepDefList(flowStepDef);
    }


    public int insertFlowStepDef(FlowStepDef flowStepDef) {
        flowStepDef.setCreateTime(DateUtils.getNowDate());
        return flowStepDefMapper.insertFlowStepDef(flowStepDef);
    }


    public int updateFlowStepDef(FlowStepDef flowStepDef) {
        flowStepDef.setUpdateTime(DateUtils.getNowDate());
        return flowStepDefMapper.updateFlowStepDef(flowStepDef);
    }


    public int deleteFlowStepDefByIds(Long[] ids) {
        return flowStepDefMapper.deleteFlowStepDefByIds(ids);
    }


    public int deleteFlowStepDefById(Long id) {
        return flowStepDefMapper.deleteFlowStepDefById(id);
    }

    /**
     * 列出可用的触发事件（tigger 包下，入参为 FlowInstance 的公共方法）
     */
    public Map<String, List<String>> listAvailableEvents() {
        return tiggerMap;
    }
}

