package com.hfits.web.controller.system.flow;

import com.hfits.common.annotation.log.Log;
import com.hfits.common.annotation.log.Module;
import com.hfits.common.core.controller.BaseController;
import com.hfits.common.core.domain.Resp;
import com.hfits.common.core.page.TableDataInfo;
import com.hfits.common.enums.BusinessType;
import com.hfits.system.workflow.domain.FlowAction;
import com.hfits.system.workflow.domain.FlowInstance;
import com.hfits.system.workflow.service.FlowInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程实例/审批
 */
@Module("流程实例")
@RestController
@RequestMapping("/flow/instance")
public class FlowInstanceController extends BaseController {
    @Autowired
    private FlowInstanceService flowInstanceService;

    @PreAuthorize("@ss.hasPermi('flow:instance:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowInstance flowInstance) {
        startPage();
        List<FlowInstance> list = flowInstanceService.selectFlowInstanceList(flowInstance);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:query')")
    @GetMapping("/{id}")
    public Resp<FlowInstance> getInfo(@PathVariable Long id) {
        return successR(flowInstanceService.selectFlowInstanceById(id));
    }

    //暂存
    @PreAuthorize("@ss.hasPermi('flow:instance:add')")
    @PostMapping("start")
    @Log(title = "发起流程", businessType = BusinessType.INSERT)
    public Resp<FlowInstance> start(@Validated @RequestBody FlowInstance flowInstance) {
        flowInstance.setApplicantId(getUserId());
        flowInstance.setCreateBy(getUsername());
        FlowInstance instance = flowInstanceService.createInstance(flowInstance);
        return successR(instance);
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:edit')")
    @PostMapping("/{id}/submit")
    @Log(title = "提交流程实例", businessType = BusinessType.UPDATE)
    public Resp<Integer> submit(@PathVariable Long id) {
        return toAjaxR(flowInstanceService.submitInstance(id));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:edit')")
    @PutMapping
    @Log(title = "修改流程实例", businessType = BusinessType.UPDATE)
    public Resp<Integer> edit(@RequestBody FlowInstance flowInstance) {
        flowInstance.setUpdateBy(getUsername());
        return toAjaxR(flowInstanceService.updateFlowInstance(flowInstance));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:remove')")
    @DeleteMapping("/{ids}")
    @Log(title = "删除流程实例", businessType = BusinessType.DELETE)
    public Resp<Integer> remove(@PathVariable Long[] ids) {
        return toAjaxR(flowInstanceService.deleteFlowInstanceByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:action')")
    @PostMapping("/{id}/action")
    @Log(title = "流程审批", businessType = BusinessType.UPDATE)
    public Resp<Integer> action(@PathVariable Long id, @Validated @RequestBody FlowAction flowAction) {
        flowAction.setInstanceId(id);
        flowAction.setOperatorId(getUserId());
        return toAjaxR(flowInstanceService.handleAction(flowAction));
    }

    @PreAuthorize("@ss.hasPermi('flow:instance:query')")
    @GetMapping("/{id}/actions")
    public Resp<List<FlowAction>> actionList(@PathVariable Long id) {
        return successR(flowInstanceService.listActions(id));
    }
}

