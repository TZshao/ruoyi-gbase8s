package com.hfits.system.workflow.service;

import com.hfits.common.utils.DateUtils;
import com.hfits.system.workflow.domain.FlowInstance;
import com.hfits.system.workflow.domain.FlowStepDef;
import com.hfits.system.workflow.mapper.FlowStepDefMapper;
import com.hfits.system.workflow.trigger.FlowTrigger;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程步骤定义Service业务层处理
 */
@Service
public class FlowStepDefService {
    private final Map<String, List<String>> tiggerMap = new HashMap<>();
    @Autowired
    private FlowStepDefMapper flowStepDefMapper;
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        // 获取所有FlowTrigger类型的bean名称
        String[] beanNames = applicationContext.getBeanNamesForType(FlowTrigger.class);

        for (String beanName : beanNames) {
            FlowTrigger trigger = applicationContext.getBean(beanName, FlowTrigger.class);
            Class<?> clazz = trigger.getClass();
            List<String> methodList = tiggerMap.computeIfAbsent(beanName, k -> new ArrayList<>());
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
                tiggerMap.remove(beanName);
            }
        }
    }

    public FlowStepDef selectFlowStepDefById(Long id) {
        return flowStepDefMapper.selectById(id);
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

