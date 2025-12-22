package com.hfits.system.workflow.mapper;

import com.hfits.system.workflow.domain.FlowInstance;

import java.util.List;

/**
 * 流程实例Mapper接口
 */
public interface FlowInstanceMapper {
    FlowInstance selectFlowInstanceById(Long id);

    List<FlowInstance> selectFlowInstanceList(FlowInstance flowInstance);

    int insertFlowInstance(FlowInstance flowInstance);

    int updateFlowInstance(FlowInstance flowInstance);

    int deleteFlowInstanceById(Long id);

    int deleteFlowInstanceByIds(Long[] ids);
}

