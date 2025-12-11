package com.ruoyi.system.mapper;

import java.util.List;
import com.hfits.system.workflow.domain.FlowAction;

/**
 * 流程动作Mapper接口
 */
public interface FlowActionMapper
{
    FlowAction selectFlowActionById(Long id);

    List<FlowAction> selectFlowActionList(FlowAction flowAction);

    List<FlowAction> selectByInstanceId(Long instanceId);

    int insertFlowAction(FlowAction flowAction);

    int deleteFlowActionById(Long id);

    int deleteFlowActionByIds(Long[] ids);
}

