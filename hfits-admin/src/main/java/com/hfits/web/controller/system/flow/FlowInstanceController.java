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
 * 流程实例
 */
@Module("流程实例")
@RestController
@RequestMapping("/flow/instance")
public class FlowInstanceController extends BaseController {
    @Autowired
    private FlowInstanceService flowInstanceService;

    /**
     * 查询流程实例列表
     *
     * @param flowInstance 流程实例查询条件
     * @return 流程实例列表
     */
    @PreAuthorize("@ss.hasPermi('flow:instance:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowInstance flowInstance) {
        startPage();
        List<FlowInstance> list = flowInstanceService.selectFlowInstanceList(flowInstance);
        return getDataTable(list);
    }

    /**
     * 根据ID获取流程实例详情
     *
     * @param id 流程实例ID
     * @return 流程实例信息
     */
    @PreAuthorize("@ss.hasPermi('flow:instance:query')")
    @GetMapping("/{id}")
    public Resp<FlowInstance> getInfo(@PathVariable Long id) {
        return successR(flowInstanceService.selectFlowInstanceById(id));
    }

    /**
     * 发起流程
     *
     * @param flowInstance 流程实例信息
     * @return 创建的流程实例
     */
    @PreAuthorize("@ss.hasPermi('flow:instance:add')")
    @PostMapping("start")
    @Log(title = "发起流程", businessType = BusinessType.INSERT)
    public Resp<FlowInstance> start(@Validated @RequestBody FlowInstance flowInstance) {
        flowInstance.setApplicantId(getUserId());
        flowInstance.setCreateBy(getUsername());
        FlowInstance instance = flowInstanceService.createInstance(flowInstance);
        return successR(instance);
    }

    /**
     * 提交流程实例
     *
     * @param id 流程实例ID
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('flow:instance:edit')")
    @PostMapping("/{id}/submit")
    @Log(title = "提交流程实例", businessType = BusinessType.UPDATE)
    public Resp<Integer> submit(@PathVariable Long id) {
        return toAjaxR(flowInstanceService.submitInstance(id));
    }

    /**
     * 修改流程实例
     *
     * @param flowInstance 流程实例信息
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('flow:instance:edit')")
    @PutMapping
    @Log(title = "修改流程实例", businessType = BusinessType.UPDATE)
    public Resp<Integer> edit(@RequestBody FlowInstance flowInstance) {
        flowInstance.setUpdateBy(getUsername());
        return toAjaxR(flowInstanceService.updateFlowInstance(flowInstance));
    }

    /**
     * 删除流程实例
     *
     * @param ids 流程实例ID数组
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('flow:instance:remove')")
    @DeleteMapping("/{ids}")
    @Log(title = "删除流程实例", businessType = BusinessType.DELETE)
    public Resp<Integer> remove(@PathVariable Long[] ids) {
        return toAjaxR(flowInstanceService.deleteFlowInstanceByIds(ids));
    }

    /**
     * 流程审批操作
     *
     * @param id 流程实例ID
     * @param flowAction 流程操作信息
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermi('flow:instance:action')")
    @PostMapping("/{id}/action")
    @Log(title = "流程审批", businessType = BusinessType.UPDATE)
    public Resp<Integer> action(@PathVariable Long id, @Validated @RequestBody FlowAction flowAction) {
        flowAction.setInstanceId(id);
        flowAction.setOperatorId(getUserId());
        return toAjaxR(flowInstanceService.handleAction(flowAction));
    }

    /**
     * 获取流程操作列表
     *
     * @param id 流程实例ID
     * @return 流程操作列表
     */
    @PreAuthorize("@ss.hasPermi('flow:instance:query')")
    @GetMapping("/{id}/actions")
    public Resp<List<FlowAction>> actionList(@PathVariable Long id) {
        return successR(flowInstanceService.listActions(id));
    }
}

