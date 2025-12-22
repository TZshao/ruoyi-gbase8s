package com.hfits.system.workflow.mapper;

import com.hfits.system.workflow.domain.FlowDef;

import java.util.List;

/**
 * 流程定义Mapper接口
 */
public interface FlowDefMapper {
    FlowDef selectFlowDefById(Long id);

    List<FlowDef> selectFlowDefList(FlowDef flowDef);

    /**
     * 根据flowCode查询流程定义列表（全部版本）
     *
     * @param flowCode 流程编号
     * @return 流程定义列表
     */
    List<FlowDef> listByFlowCode(String flowCode);

    int insertFlowDef(FlowDef flowDef);

    int updateFlowDef(FlowDef flowDef);

    int deleteFlowDefById(Long id);

    int deleteFlowDefByIds(Long[] ids);
}

