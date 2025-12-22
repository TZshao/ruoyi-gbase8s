package com.hfits.system.workflow.mapper;

import com.hfits.system.workflow.domain.FlowAction;

import java.util.List;

/**
 * 流程动作Mapper接口
 */
public interface FlowActionMapper {
    FlowAction selectFlowActionById(Long id);

    List<FlowAction> selectFlowActionList(FlowAction flowAction);

    List<FlowAction> selectByInstanceId(Long instanceId);

    int insertFlowAction(FlowAction flowAction);

    int deleteFlowActionById(Long id);

    int deleteFlowActionByIds(Long[] ids);
}

