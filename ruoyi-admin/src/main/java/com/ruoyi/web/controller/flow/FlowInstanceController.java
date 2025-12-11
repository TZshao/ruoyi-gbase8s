package com.ruoyi.web.controller.flow;

import com.hfits.system.workflow.domain.FlowAction;
import com.hfits.system.workflow.domain.FlowInstance;
import com.hfits.system.workflow.service.FlowInstanceService;
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
 * 流程实例/审批
 */
@RestController
@RequestMapping("/flow/instance")
public class FlowInstanceController extends BaseController
{
    @Autowired
    private FlowInstanceService flowInstanceService;

    @PreAuthorize("@ss.hasPermi('flow:instance:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowInstance flowInstance)
    {
        startPage();
        List<FlowInstance> list = flowInstanceService.selectFlowInstanceList(flowInstance);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(flowInstanceService.selectFlowInstanceById(id));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:add')")
    @Log(title = "流程实例", businessType = BusinessType.INSERT)
    @PostMapping("/start")
    public AjaxResult start(@Validated @RequestBody FlowInstance flowInstance)
    {
        flowInstance.setApplicantId(getUserId());
        flowInstance.setCreateBy(getUsername());
        return toAjax(flowInstanceService.createInstance(flowInstance));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:edit')")
    @Log(title = "流程实例", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FlowInstance flowInstance)
    {
        flowInstance.setUpdateBy(getUsername());
        return toAjax(flowInstanceService.updateFlowInstance(flowInstance));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:remove')")
    @Log(title = "流程实例", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(flowInstanceService.deleteFlowInstanceByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:action')")
    @Log(title = "流程审批", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/action")
    public AjaxResult action(@PathVariable Long id, @Validated @RequestBody FlowAction flowAction)
    {
        flowAction.setInstanceId(id);
        flowAction.setOperatorId(getUserId());
        return toAjax(flowInstanceService.handleAction(flowAction));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:query')")
    @GetMapping("/{id}/actions")
    public AjaxResult actionList(@PathVariable Long id)
    {
        return success(flowInstanceService.listActions(id));
    }
}

