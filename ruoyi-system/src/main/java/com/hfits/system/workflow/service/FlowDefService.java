package com.hfits.system.workflow.service;

import com.ruoyi.common.utils.DateUtils;
import com.hfits.system.workflow.domain.FlowDef;
import com.ruoyi.system.mapper.FlowDefMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程定义Service业务层处理
 */
@Service
public class FlowDefService {
    @Autowired
    private FlowDefMapper flowDefMapper;


    public FlowDef selectFlowDefById(Long id) {
        return flowDefMapper.selectFlowDefById(id);
    }


    public List<FlowDef> selectFlowDefList(FlowDef flowDef) {
        return flowDefMapper.selectFlowDefList(flowDef);
    }


    public int insertFlowDef(FlowDef flowDef) {
        if (StringUtils.isBlank(flowDef.getFlowCode())) {
            flowDef.setFlowCode(generateFlowCode());
        }
        if (flowDef.getVersion() == null) {
            flowDef.setVersion(1);
        }
        flowDef.setCreateTime(DateUtils.getNowDate());
        return flowDefMapper.insertFlowDef(flowDef);
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

