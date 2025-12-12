package com.hfits.system.workflow.service;

import static com.hfits.system.workflow.service.FlowDefService.CLOSE_STEP_CODE;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hfits.system.workflow.domain.FlowAction;
import com.hfits.system.workflow.domain.FlowDef;
import com.hfits.system.workflow.domain.FlowInstance;
import com.hfits.system.workflow.domain.FlowStatus;
import com.hfits.system.workflow.domain.FlowStepDef;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.FlowActionMapper;
import com.ruoyi.system.mapper.FlowDefMapper;
import com.ruoyi.system.mapper.FlowInstanceMapper;
import com.ruoyi.system.mapper.FlowStepDefMapper;

/**
 * 流程实例Service业务层处理
 */
@Service
public class FlowInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(FlowInstanceService.class);
    @Autowired
    private FlowInstanceMapper flowInstanceMapper;

    @Autowired
    private FlowActionMapper flowActionMapper;

    @Autowired
    private FlowStepDefMapper flowStepDefMapper;

    @Autowired
    private FlowDefMapper flowDefMapper;

    @Autowired
    private ApplicationContext applicationContext;

    public FlowInstance selectFlowInstanceById(Long id) {
        return flowInstanceMapper.selectFlowInstanceById(id);
    }


    public List<FlowInstance> selectFlowInstanceList(FlowInstance flowInstance) {
        return flowInstanceMapper.selectFlowInstanceList(flowInstance);
    }


    @Transactional(rollbackFor = Exception.class)
    public FlowInstance createInstance(FlowInstance flowInstance) {
        // 获取流程定义信息，保存冗余字段
        FlowDef flowDef = flowDefMapper.selectFlowDefById(flowInstance.getFlowId());
        if (flowDef == null) {
            throw new ServiceException("流程定义不存在");
        }
        flowInstance.setFlowCode(flowDef.getFlowCode());
        flowInstance.setFlowVersion(flowDef.getVersion());

        // 创建时状态为PENDING，步骤码为RISE，用户可以修改和提交
        flowInstance.setStatus(FlowStatus.PENDING.name());
        flowInstance.setCurrentStepCode("RISE");
        flowInstance.setCreateTime(DateUtils.getNowDate());
        flowInstanceMapper.insertFlowInstance(flowInstance);
        return flowInstance;
    }


    public int updateFlowInstance(FlowInstance flowInstance) {
        // 只有PENDING状态的实例才能修改
        if (flowInstance.getId() != null) {
            FlowInstance existing = flowInstanceMapper.selectFlowInstanceById(flowInstance.getId());
            if (existing != null && !FlowStatus.PENDING.name().equalsIgnoreCase(existing.getStatus())) {
                throw new ServiceException("只有待提交状态的流程实例才能修改");
            }
        }
        flowInstance.setUpdateTime(DateUtils.getNowDate());
        return flowInstanceMapper.updateFlowInstance(flowInstance);
    }

    /**
     * 提交流程实例，将PENDING状态转为RUNNING，步骤码从RISE转为第一步
     */
    @Transactional(rollbackFor = Exception.class)
    public int submitInstance(Long instanceId) {
        FlowInstance instance = flowInstanceMapper.selectFlowInstanceById(instanceId);
        if (instance == null) {
            throw new ServiceException("流程实例不存在");
        }
        if (!FlowStatus.PENDING.name().equalsIgnoreCase(instance.getStatus())) {
            throw new ServiceException("只有待提交状态的流程实例才能提交");
        }

        // 获取第一步步骤码
        FlowStepDef currentStep = flowStepDefMapper.selectByFlowIdAndCode(instance.getFlowId(), instance.getCurrentStepCode());
        // 更新状态为RUNNING，步骤码转为第一步
        instance.setStatus(FlowStatus.RUNNING.name());
        instance.setCurrentStepCode(currentStep.getNextOnPass());
        instance.setUpdateTime(DateUtils.getNowDate());
        return flowInstanceMapper.updateFlowInstance(instance);
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
        // 只有RUNNING状态的实例才能审批
        if (!FlowStatus.RUNNING.name().equalsIgnoreCase(instance.getStatus())) {
            throw new ServiceException("只有运行中的流程实例才能审批");
        }
        FlowStepDef currentStep = flowStepDefMapper.selectByFlowIdAndCode(instance.getFlowId(), instance.getCurrentStepCode());
        if (currentStep == null) {
            throw new ServiceException("当前步骤配置缺失");
        }

        String nextCode = Boolean.TRUE.equals(action.getPass()) ? currentStep.getNextOnPass() : currentStep.getNextOnReject();

        String newStatus;
        String newStep;
        boolean isClose = CLOSE_STEP_CODE.equalsIgnoreCase(StringUtils.trim(nextCode));
        if (isClose) {
            newStatus = FlowStatus.CLOSED.name();
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
        // 如果审批时提供了表单数据，则更新实例的表单数据
        if (StringUtils.isNotBlank(action.getFormData())) {
            instance.setFormData(action.getFormData());
        }
        instance.setUpdateTime(DateUtils.getNowDate());
        flowInstanceMapper.updateFlowInstance(instance);

        // 触发事件
        String eventKey = Boolean.TRUE.equals(action.getPass()) ? currentStep.getEventOnPass() : currentStep.getEventOnReject();
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
        String beanName = parts[0];
        String methodName = parts[1];
        try {
            // 通过beanName获取bean
            Object bean = applicationContext.getBean(beanName);
            Method target = bean.getClass().getMethod(methodName, FlowInstance.class);
            target.invoke(bean, instance);
        } catch (Exception e) {
            logger.error("触发事件失败: " + e.getMessage());
        }
    }
}

