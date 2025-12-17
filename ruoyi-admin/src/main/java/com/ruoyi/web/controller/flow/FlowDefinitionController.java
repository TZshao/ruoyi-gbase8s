package com.ruoyi.web.controller.flow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfits.system.workflow.domain.FlowDef;
import com.hfits.system.workflow.service.FlowDefService;
import com.ruoyi.common.annotation.log.Module;
import com.ruoyi.common.annotation.log.PostMappingLog;
import com.ruoyi.common.annotation.log.PutMappingLog;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.Resp;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 流程定义管理
 */
@Module("流程管理")
@RestController
@RequestMapping("/flow/definition")
public class FlowDefinitionController extends BaseController {
    @Autowired
    private FlowDefService flowDefService;

    @PreAuthorize("@ss.hasPermi('flow:def:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowDef flowDef) {
        startPage();
        List<FlowDef> list = flowDefService.selectFlowDefList(flowDef);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('flow:def:query')")
    @GetMapping("/listByFlowCode")
    public Resp<List<FlowDef>> listByFlowCode(@RequestParam String flowCode, @RequestParam(required = false, defaultValue = "false") boolean latestPublished) {
        List<FlowDef> list = flowDefService.listByFlowCode(flowCode, latestPublished);
        return successR(list);
    }

    @PreAuthorize("@ss.hasPermi('flow:def:query')")
    @GetMapping("/{id}")
    public Resp<FlowDef> getInfo(@PathVariable Long id) {
        return successR(flowDefService.selectFlowDefById(id));
    }

    @PreAuthorize("@ss.hasPermi('flow:def:add')")
    @PostMappingLog(title = "流程定义", businessType = BusinessType.INSERT)
    public Resp<?> add(@Validated @RequestBody FlowDef flowDef) {
        flowDef.setCreateBy(getUsername());
        return toAjaxR(flowDefService.insertFlowDef(flowDef));
    }

    @PreAuthorize("@ss.hasPermi('flow:def:edit')")
    @PutMappingLog(title = "流程修改", businessType = BusinessType.UPDATE)
    public Resp<?> edit(@Validated @RequestBody FlowDef flowDef) {
        flowDef.setUpdateBy(getUsername());
        return toAjaxR(flowDefService.updateFlowDef(flowDef));
    }

    @PreAuthorize("@ss.hasPermi('flow:def:edit')")
    @PostMappingLog(value = "/publish/{id}", title = "流程发布", businessType = BusinessType.UPDATE)
    public Resp<?> publish(@PathVariable Long id) {
        return toAjaxR(flowDefService.publishFlowDef(id));
    }

    @PreAuthorize("@ss.hasPermi('flow:def:add')")
    @PostMappingLog(value = "/iterate/{id}", title = "流程迭代", businessType = BusinessType.INSERT)
    public Resp<Long> iterate(@PathVariable Long id) {
        Long newId = flowDefService.iterateFlowDefVersion(id, getUsername());
        return successR(newId);
    }
}

