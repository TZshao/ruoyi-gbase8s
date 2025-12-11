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

import java.util.List;

/**
 * 流程实例Service业务层处理
 */
@Service
public class FlowInstanceService
{
    @Autowired
    private FlowInstanceMapper flowInstanceMapper;

    @Autowired
    private FlowActionMapper flowActionMapper;

    @Autowired
    private FlowStepDefMapper flowStepDefMapper;


    public FlowInstance selectFlowInstanceById(Long id)
    {
        return flowInstanceMapper.selectFlowInstanceById(id);
    }


    public List<FlowInstance> selectFlowInstanceList(FlowInstance flowInstance)
    {
        return flowInstanceMapper.selectFlowInstanceList(flowInstance);
    }


    @Transactional(rollbackFor = Exception.class)
    public int createInstance(FlowInstance flowInstance)
    {
        String firstStep = flowStepDefMapper.selectFirstStepCode(flowInstance.getFlowId());
        if (StringUtils.isEmpty(firstStep))
        {
            throw new ServiceException("流程未配置步骤，请先配置步骤");
        }
        flowInstance.setStatus(FlowStatus.RUNNING.name());
        flowInstance.setCurrentStepCode(firstStep);
        flowInstance.setCreateTime(DateUtils.getNowDate());
        return flowInstanceMapper.insertFlowInstance(flowInstance);
    }


    public int updateFlowInstance(FlowInstance flowInstance)
    {
        flowInstance.setUpdateTime(DateUtils.getNowDate());
        return flowInstanceMapper.updateFlowInstance(flowInstance);
    }


    public int deleteFlowInstanceByIds(Long[] ids)
    {
        return flowInstanceMapper.deleteFlowInstanceByIds(ids);
    }


    public int deleteFlowInstanceById(Long id)
    {
        return flowInstanceMapper.deleteFlowInstanceById(id);
    }


    @Transactional(rollbackFor = Exception.class)
    public int handleAction(FlowAction action)
    {
        FlowInstance instance = flowInstanceMapper.selectFlowInstanceById(action.getInstanceId());
        if (instance == null)
        {
            throw new ServiceException("流程实例不存在");
        }
        if (!FlowStatus.RUNNING.name().equalsIgnoreCase(instance.getStatus()))
        {
            throw new ServiceException("流程已结束或不可审批");
        }
        FlowStepDef currentStep = flowStepDefMapper.selectByFlowIdAndCode(instance.getFlowId(), instance.getCurrentStepCode());
        if (currentStep == null)
        {
            throw new ServiceException("当前步骤配置缺失");
        }

        String nextCode = "PASS".equalsIgnoreCase(action.getAction()) ? currentStep.getNextOnPass() : currentStep.getNextOnReject();
        String firstStep = flowStepDefMapper.selectFirstStepCode(instance.getFlowId());

        String newStatus;
        String newStep;
        if (StringUtils.isEmpty(nextCode) || "FINISH".equalsIgnoreCase(nextCode))
        {
            newStatus = FlowStatus.PASS.name();
            newStep = null;
        }
        else if ("START".equalsIgnoreCase(nextCode))
        {
            newStatus = FlowStatus.REJECT.name();
            newStep = firstStep;
        }
        else
        {
            newStatus = FlowStatus.RUNNING.name();
            newStep = nextCode;
        }

        action.setStepCode(instance.getCurrentStepCode());
        action.setActionTime(DateUtils.getNowDate());
        flowActionMapper.insertFlowAction(action);

        instance.setStatus(newStatus);
        instance.setCurrentStepCode(newStep);
        instance.setUpdateTime(DateUtils.getNowDate());
        flowInstanceMapper.updateFlowInstance(instance);
        return 1;
    }


    public List<FlowAction> listActions(Long instanceId)
    {
        return flowActionMapper.selectByInstanceId(instanceId);
    }
}

