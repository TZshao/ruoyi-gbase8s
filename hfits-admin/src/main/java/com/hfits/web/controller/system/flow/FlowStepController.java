package com.hfits.web.controller.system.flow;

import com.hfits.common.annotation.log.Log;
import com.hfits.common.core.controller.BaseController;
import com.hfits.common.core.domain.Resp;
import com.hfits.common.core.page.TableDataInfo;
import com.hfits.common.enums.BusinessType;
import com.hfits.system.workflow.domain.FlowStepDef;
import com.hfits.system.workflow.service.FlowStepDefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程步骤配置
 */
@RestController
@RequestMapping("/flow/step")
public class FlowStepController extends BaseController {
    @Autowired
    private FlowStepDefService flowStepDefService;

    @PreAuthorize("@ss.hasPermi('flow:step:list')")
    @GetMapping("/triggers")
    public Resp<Map<String, List<String>>> triggerOptions() {
        return successR(flowStepDefService.listAvailableEvents());
    }

    @PreAuthorize("@ss.hasPermi('flow:step:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowStepDef flowStepDef) {
        startPage();
        List<FlowStepDef> list = flowStepDefService.selectFlowStepDefList(flowStepDef);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flow:step:query')")
    @GetMapping("/{id}")
    public Resp<FlowStepDef> getInfo(@PathVariable Long id) {
        return successR(flowStepDefService.selectFlowStepDefById(id));
    }

    @PreAuthorize("@ss.hasPermi('flow:step:add')")
    @Log(title = "流程步骤", businessType = BusinessType.INSERT)
    @PostMapping
    public Resp<Integer> add(@Validated @RequestBody FlowStepDef flowStepDef) {
        flowStepDef.setCreateBy(getUsername());
        return toAjaxR(flowStepDefService.insertFlowStepDef(flowStepDef));
    }

    @PreAuthorize("@ss.hasPermi('flow:step:edit')")
    @Log(title = "流程步骤", businessType = BusinessType.UPDATE)
    @PutMapping
    public Resp<Integer> edit(@Validated @RequestBody FlowStepDef flowStepDef) {
        flowStepDef.setUpdateBy(getUsername());
        return toAjaxR(flowStepDefService.updateFlowStepDef(flowStepDef));
    }

    @PreAuthorize("@ss.hasPermi('flow:step:remove')")
    @Log(title = "流程步骤", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public Resp<Integer> remove(@PathVariable Long[] ids) {
        return toAjaxR(flowStepDefService.deleteFlowStepDefByIds(ids));
    }
}

