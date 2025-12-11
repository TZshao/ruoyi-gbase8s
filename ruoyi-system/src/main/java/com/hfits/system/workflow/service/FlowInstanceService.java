package com.hfits.system.workflow.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.hfits.system.workflow.domain.FlowAction;
import com.hfits.system.workflow.domain.FlowInstance;
import com.hfits.system.workflow.domain.FlowStatus;
import com.hfits.system.workflow.domain.FlowStepDef;
import com.ruoyi.system.mapper.FlowActionMapper;
import com.ruoyi.system.mapper.FlowInstanceMapper;
import com.ruoyi.system.mapper.FlowStepDefMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.List;

/**
 * 流程实例Service业务层处理
 */
@Service
public class FlowInstanceService {
    @Autowired
    private FlowInstanceMapper flowInstanceMapper;

    @Autowired
    private FlowActionMapper flowActionMapper;

    @Autowired
    private FlowStepDefMapper flowStepDefMapper;

    @Autowired
    private ApplicationContext applicationContext;

    private static final String CLOSE_CODE = "close";


    public FlowInstance selectFlowInstanceById(Long id) {
        return flowInstanceMapper.selectFlowInstanceById(id);
    }


    public List<FlowInstance> selectFlowInstanceList(FlowInstance flowInstance) {
        return flowInstanceMapper.selectFlowInstanceList(flowInstance);
    }


    @Transactional(rollbackFor = Exception.class)
    public int createInstance(FlowInstance flowInstance) {
        String firstStep = flowStepDefMapper.selectFirstStepCode(flowInstance.getFlowId());
        if (StringUtils.isEmpty(firstStep)) {
            throw new ServiceException("流程未配置步骤，请先配置步骤");
        }
        flowInstance.setStatus(FlowStatus.RUNNING.name());
        flowInstance.setCurrentStepCode(firstStep);
        flowInstance.setCreateTime(DateUtils.getNowDate());
        return flowInstanceMapper.insertFlowInstance(flowInstance);
    }


    public int updateFlowInstance(FlowInstance flowInstance) {
        flowInstance.setUpdateTime(DateUtils.getNowDate());
        return flowInstanceMapper.updateFlowInstance(flowInstance);
    }


    public int deleteFlowInstanceByIds(Long[] ids) {
        return flowInstanceMapper.deleteFlowInstanceByIds(ids);
    }


    public int deleteFlowInstanceById(Long id) {
        return flowInstanceMapper.deleteFlowInstanceById(id);
    }


    @Transactional(rollbackFor = Exception.class)
    public int handleAction(FlowAction action) {
        FlowInstance instance = flowInstanceMapper.selectFlowInstanceById(action.getInstanceId());
        if (instance == null) {
            throw new ServiceException("流程实例不存在");
        }
        if (!FlowStatus.CLOSED.name().equalsIgnoreCase(instance.getStatus())) {
            throw new ServiceException("流程已结束或不可审批");
        }
        FlowStepDef currentStep = flowStepDefMapper.selectByFlowIdAndCode(instance.getFlowId(), instance.getCurrentStepCode());
        if (currentStep == null) {
            throw new ServiceException("当前步骤配置缺失");
        }

        String nextCode = "PASS".equalsIgnoreCase(action.getAction()) ? currentStep.getNextOnPass() : currentStep.getNextOnReject();

        String newStatus;
        String newStep;
        boolean isClose = StringUtils.isNotEmpty(nextCode) && CLOSE_CODE.equalsIgnoreCase(nextCode.trim());
        if (isClose) {
            newStatus = CLOSE_CODE;
            newStep = instance.getCurrentStepCode();
        } else {
            newStatus = FlowStatus.RUNNING.name();
            newStep = StringUtils.isBlank(nextCode) ? instance.getCurrentStepCode() : nextCode;
        }

        action.setStepCode(instance.getCurrentStepCode());
        action.setActionTime(DateUtils.getNowDate());
        flowActionMapper.insertFlowAction(action);

        instance.setStatus(newStatus);
        instance.setCurrentStepCode(newStep);
        instance.setUpdateTime(DateUtils.getNowDate());
        flowInstanceMapper.updateFlowInstance(instance);

        // 触发事件
        String eventKey = "PASS".equalsIgnoreCase(action.getAction()) ? currentStep.getEventOnPass() : currentStep.getEventOnReject();
        invokeEvent(eventKey, instance);
        return 1;
    }


    public List<FlowAction> listActions(Long instanceId) {
        return flowActionMapper.selectByInstanceId(instanceId);
    }

    /**
     * 调用 tigger 包下的事件
     */
    private void invokeEvent(String eventKey, FlowInstance instance) {
        if (StringUtils.isBlank(eventKey)) {
            return;
        }
        String[] parts = eventKey.split("\\.");
        if (parts.length != 2) {
            return;
        }
        String className = parts[0];
        String methodName = parts[1];
        String fullClassName = "com.hfits.system.workflow.tigger." + className;
        try {
            Class<?> clazz = Class.forName(fullClassName);
            Method target = clazz.getMethod(methodName, FlowInstance.class);
            Object bean;
            try {
                bean = applicationContext.getBean(clazz);
            } catch (Exception e) {
                bean = clazz.getDeclaredConstructor().newInstance();
            }
            target.invoke(bean, instance);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException ignored) {
            // 静默处理，无需影响主流程
        }
    }
}

