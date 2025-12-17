package com.ruoyi.system.mapper;

import java.util.List;
import com.hfits.system.workflow.domain.FlowStepDef;

/**
 * 流程步骤定义Mapper接口
 */
public interface FlowStepDefMapper
{
    FlowStepDef selectById(Long id);

    FlowStepDef selectByFlowIdAndCode(Long flowId, String stepCode);
    //下一步骤
    FlowStepDef selectNextStep(Long flowId, String stepCode);

    String selectFirstStepCode(Long flowId);

    List<FlowStepDef> selectFlowStepDefList(FlowStepDef flowStepDef);

    int insertFlowStepDef(FlowStepDef flowStepDef);

    int updateFlowStepDef(FlowStepDef flowStepDef);

    int deleteFlowStepDefById(Long id);

    int deleteFlowStepDefByIds(Long[] ids);
}

