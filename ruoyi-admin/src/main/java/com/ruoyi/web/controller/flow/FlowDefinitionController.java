package com.ruoyi.web.controller.flow;

import com.hfits.system.workflow.domain.FlowDef;
import com.hfits.system.workflow.service.FlowDefService;
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
 * 流程定义管理
 */
@RestController
@RequestMapping("/flow/definition")
public class FlowDefinitionController extends BaseController
{
    @Autowired
    private FlowDefService flowDefService;

    @PreAuthorize("@ss.hasPermi('flow:def:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowDef flowDef)
    {
        startPage();
        List<FlowDef> list = flowDefService.selectFlowDefList(flowDef);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flow:def:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(flowDefService.selectFlowDefById(id));
    }

    @PreAuthorize("@ss.hasPermi('flow:def:add')")
    @Log(title = "流程定义", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FlowDef flowDef)
    {
        flowDef.setCreateBy(getUsername());
        return toAjax(flowDefService.insertFlowDef(flowDef));
    }

    @PreAuthorize("@ss.hasPermi('flow:def:edit')")
    @Log(title = "流程定义", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FlowDef flowDef)
    {
        flowDef.setUpdateBy(getUsername());
        return toAjax(flowDefService.updateFlowDef(flowDef));
    }

    @PreAuthorize("@ss.hasPermi('flow:def:remove')")
    @Log(title = "流程定义", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(flowDefService.deleteFlowDefByIds(ids));
    }
}

