package com.hfits.system.workflow.service;

import com.ruoyi.common.utils.DateUtils;
import com.hfits.system.workflow.domain.FlowStepDef;
import com.ruoyi.system.mapper.FlowStepDefMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程步骤定义Service业务层处理
 */
@Service
public class FlowStepDefService
{
    @Autowired
    private FlowStepDefMapper flowStepDefMapper;


    public FlowStepDef selectFlowStepDefById(Long id)
    {
        return flowStepDefMapper.selectFlowStepDefById(id);
    }


    public List<FlowStepDef> selectFlowStepDefList(FlowStepDef flowStepDef)
    {
        return flowStepDefMapper.selectFlowStepDefList(flowStepDef);
    }


    public int insertFlowStepDef(FlowStepDef flowStepDef)
    {
        flowStepDef.setCreateTime(DateUtils.getNowDate());
        return flowStepDefMapper.insertFlowStepDef(flowStepDef);
    }


    public int updateFlowStepDef(FlowStepDef flowStepDef)
    {
        flowStepDef.setUpdateTime(DateUtils.getNowDate());
        return flowStepDefMapper.updateFlowStepDef(flowStepDef);
    }


    public int deleteFlowStepDefByIds(Long[] ids)
    {
        return flowStepDefMapper.deleteFlowStepDefByIds(ids);
    }


    public int deleteFlowStepDefById(Long id)
    {
        return flowStepDefMapper.deleteFlowStepDefById(id);
    }
}

