package com.ruoyi.web.controller.flow;

import com.hfits.system.workflow.domain.FlowStepDef;
import com.hfits.system.workflow.service.FlowStepDefService;
import com.ruoyi.common.annotation.log.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程步骤配置
 */
@RestController
@RequestMapping("/flow/step")
public class FlowStepController extends BaseController
{
    @Autowired
    private FlowStepDefService flowStepDefService;

    @PreAuthorize("@ss.hasPermi('flow:step:list')")
    @GetMapping("/triggers")
    public AjaxResult triggerOptions()
    {
        return success(flowStepDefService.listAvailableEvents());
    }

    @PreAuthorize("@ss.hasPermi('flow:step:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowStepDef flowStepDef)
    {
        startPage();
        List<FlowStepDef> list = flowStepDefService.selectFlowStepDefList(flowStepDef);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flow:step:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(flowStepDefService.selectFlowStepDefById(id));
    }

    @PreAuthorize("@ss.hasPermi('flow:step:add')")
    @Log(title = "流程步骤", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FlowStepDef flowStepDef)
    {
        flowStepDef.setCreateBy(getUsername());
        return toAjax(flowStepDefService.insertFlowStepDef(flowStepDef));
    }

    @PreAuthorize("@ss.hasPermi('flow:step:edit')")
    @Log(title = "流程步骤", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FlowStepDef flowStepDef)
    {
        flowStepDef.setUpdateBy(getUsername());
        return toAjax(flowStepDefService.updateFlowStepDef(flowStepDef));
    }

    @PreAuthorize("@ss.hasPermi('flow:step:remove')")
    @Log(title = "流程步骤", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(flowStepDefService.deleteFlowStepDefByIds(ids));
    }
}

