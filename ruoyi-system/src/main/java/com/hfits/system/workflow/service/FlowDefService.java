package com.hfits.system.workflow.service;

import com.ruoyi.common.utils.DateUtils;
import com.hfits.system.workflow.domain.FlowDef;
import com.hfits.system.workflow.domain.FlowStepDef;
import com.ruoyi.system.mapper.FlowDefMapper;
import com.ruoyi.system.mapper.FlowStepDefMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 流程定义Service业务层处理
 */
@Service
public class FlowDefService {
    @Autowired
    private FlowDefMapper flowDefMapper;

    @Autowired
    private FlowStepDefMapper flowStepDefMapper;


    public FlowDef selectFlowDefById(Long id) {
        return flowDefMapper.selectFlowDefById(id);
    }


    public List<FlowDef> selectFlowDefList(FlowDef flowDef) {
        return flowDefMapper.selectFlowDefList(flowDef);
    }


    @Transactional(rollbackFor = Exception.class)
    public int insertFlowDef(FlowDef flowDef) {
        if (StringUtils.isBlank(flowDef.getFlowCode())) {
            flowDef.setFlowCode(generateFlowCode());
        }
        if (flowDef.getVersion() == null) {
            flowDef.setVersion(1);
        }
        flowDef.setCreateTime(DateUtils.getNowDate());
        int rows = flowDefMapper.insertFlowDef(flowDef);

        // 默认生成发起步骤，保证流程可用
        FlowStepDef startStep = new FlowStepDef();
        startStep.setFlowId(flowDef.getId());
        startStep.setStepCode("RISE");
        startStep.setStepName("发起申请");
        startStep.setOrderNum(1);
        startStep.setCreateBy(flowDef.getCreateBy());
        startStep.setCreateTime(DateUtils.getNowDate());
        flowStepDefMapper.insertFlowStepDef(startStep);
        return rows;
    }


    public int updateFlowDef(FlowDef flowDef) {
        FlowDef exist = flowDefMapper.selectFlowDefById(flowDef.getId());
        if (exist != null) {
            flowDef.setFlowCode(exist.getFlowCode());
            Integer newVersion = exist.getVersion() == null ? 1 : exist.getVersion() + 1;
            flowDef.setVersion(newVersion);
        }
        flowDef.setUpdateTime(DateUtils.getNowDate());
        return flowDefMapper.updateFlowDef(flowDef);
    }


    public int deleteFlowDefByIds(Long[] ids) {
        return flowDefMapper.deleteFlowDefByIds(ids);
    }


    public int deleteFlowDefById(Long id) {
        return flowDefMapper.deleteFlowDefById(id);
    }

    private String generateFlowCode() {
        return "FLOW" + DateUtils.dateTimeNow("yyyyMMddHHmmss");
    }
}

