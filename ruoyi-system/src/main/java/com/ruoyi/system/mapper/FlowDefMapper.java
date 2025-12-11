package com.ruoyi.system.mapper;

import java.util.List;
import com.hfits.system.workflow.domain.FlowDef;

/**
 * 流程定义Mapper接口
 */
public interface FlowDefMapper
{
    FlowDef selectFlowDefById(Long id);

    List<FlowDef> selectFlowDefList(FlowDef flowDef);

    int insertFlowDef(FlowDef flowDef);

    int updateFlowDef(FlowDef flowDef);

    int deleteFlowDefById(Long id);

    int deleteFlowDefByIds(Long[] ids);
}

