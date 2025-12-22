package com.hfits.web.controller.system.flow;

import com.hfits.common.annotation.log.Log;
import com.hfits.common.annotation.log.Module;
import com.hfits.common.core.controller.BaseController;
import com.hfits.common.core.domain.Resp;
import com.hfits.common.core.page.TableDataInfo;
import com.hfits.common.enums.BusinessType;
import com.hfits.system.workflow.domain.FlowDef;
import com.hfits.system.workflow.service.FlowDefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义
 */
@Module("流程管理")
@RestController
@RequestMapping("/flow/definition")
public class FlowDefinitionController extends BaseController {
    @Autowired
    private FlowDefService flowDefService;

    /**
     * 查询列表
     * @param flowDef
     */
    @PreAuthorize("@ss.hasPermi('flow:def:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowDef flowDef) {
        startPage();
        List<FlowDef> list = flowDefService.selectFlowDefList(flowDef);
        return getDataTable(list);
    }

    /**
     * 查询流程编码
     * @param flowCode 流程编码
     * @param latestPublished 是最新发布流程
     * @return
     */
    @PreAuthorize("@ss.hasPermi('flow:def:query')")
    @GetMapping("/listByFlowCode")
    public Resp<List<FlowDef>> listByFlowCode(@RequestParam String flowCode,
                                              @RequestParam(required = false, defaultValue = "false") boolean latestPublished) {
        List<FlowDef> list = flowDefService.listByFlowCode(flowCode, latestPublished);
        return successR(list);
    }

    /**
     * 查询流程详情
     * @param id
     */
    @PreAuthorize("@ss.hasPermi('flow:def:query')")
    @GetMapping("/{id}")
    public Resp<FlowDef> getInfo(@PathVariable Long id) {
        return successR(flowDefService.selectFlowDefById(id));
    }

    /**
     * 新增流程
     * @param flowDef 新增对象
     * @return
     */
    @PreAuthorize("@ss.hasPermi('flow:def:add')")
    @PostMapping
    @Log(title = "流程定义", businessType = BusinessType.INSERT)
    public Resp<?> add(@Validated @RequestBody FlowDef flowDef) {
        flowDef.setCreateBy(getUsername());
        return toAjaxR(flowDefService.insertFlowDef(flowDef));
    }


    /**
     * 修改流程
     * @param flowDef
     * @return
     */
    @PreAuthorize("@ss.hasPermi('flow:def:edit')")
    @PutMapping
    @Log(title = "流程修改", businessType = BusinessType.UPDATE)
    public Resp<?> edit(@Validated @RequestBody FlowDef flowDef) {
        flowDef.setUpdateBy(getUsername());
        return toAjaxR(flowDefService.updateFlowDef(flowDef));
    }

    /**
     * 发布流程
     * @param id
     * @return
     */
    @PreAuthorize("@ss.hasPermi('flow:def:edit')")
    @PostMapping("/publish/{id}")
    @Log(title = "流程发布", businessType = BusinessType.UPDATE)
    public Resp<?> publish(@PathVariable Long id) {
        return toAjaxR(flowDefService.publishFlowDef(id));
    }

    /**
     * 迭代流程
     * @param id
     * @return
     */
    @PreAuthorize("@ss.hasPermi('flow:def:add')")
    @PostMapping("/iterate/{id}")
    @Log(title = "流程迭代", businessType = BusinessType.INSERT)
    public Resp<Long> iterate(@PathVariable Long id) {
        Long newId = flowDefService.iterateFlowDefVersion(id, getUsername());
        return successR(newId);
    }
}

