package com.hfits.system.workflow.service;

import com.hfits.common.utils.DateUtils;
import com.hfits.system.workflow.domain.FlowDef;
import com.hfits.system.workflow.domain.FlowStepDef;
import com.hfits.system.workflow.mapper.FlowDefMapper;
import com.hfits.system.workflow.mapper.FlowStepDefMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 流程定义Service业务层处理
 */
@Service
public class FlowDefService {
    public static final Integer START_STEP = 1;
    public static final String START_STEP_CODE = "RISE";
    public static final String START_STEP_NAME = "发起申请";

    public static final Integer APPROVE_STEP = 2;
    public static final String APPROVE_STEP_CODE = "APPROVE";
    public static final String APPROVE_STEP_NAME = "申请审批 1";

    public static final Integer CLOSE_STEP = 99;
    public static final String CLOSE_STEP_CODE = "CLOSED";
    public static final String CLOSE_STEP_NAME = "流程关闭";

    @Autowired
    private FlowDefMapper flowDefMapper;
    @Autowired
    private FlowStepDefMapper flowStepDefMapper;

    public FlowDef selectFlowDefById(Long id) {
        return flowDefMapper.selectFlowDefById(id);
    }


    public List<FlowDef> selectFlowDefList(FlowDef flowDef) {
        return flowDefMapper.selectFlowDefList(flowDef);
    }

    /**
     * 根据flowCode查询流程定义列表（全部版本/最新版本）
     *
     * @param flowCode        流程编号
     * @param latestPublished 是否查询最新发布版本
     * @return 流程定义列表
     */
    public List<FlowDef> listByFlowCode(String flowCode, boolean latestPublished) {
        List<FlowDef> flowDefList = flowDefMapper.listByFlowCode(flowCode);
        if (!latestPublished || flowDefList.isEmpty()) {
            return flowDefList; //全部版本
        }

        FlowDef flowDef = flowDefList.get(0); //最新版本：发布状态才可用，否则视为没有可用流程
        if (flowDef.isPublish()) {
            return Collections.singletonList(flowDef);
        }
        return new ArrayList<>();
    }


    @Transactional(rollbackFor = Exception.class)
    public int insertFlowDef(FlowDef flowDef) {
        flowDef.setVersion(1);
        flowDef.setCreateTime(DateUtils.getNowDate());
        flowDef.setPublish(false);
        int rows = flowDefMapper.insertFlowDef(flowDef);

        // 默认生成发起/申请/关闭步骤，保证流程可用
        FlowStepDef startStep = new FlowStepDef();
        startStep.setFlowId(flowDef.getId()); //mybatis自动回填的id
        startStep.setStepCode(START_STEP_CODE);
        startStep.setStepName(START_STEP_NAME);
        startStep.setNextOnPass(APPROVE_STEP_CODE);
        startStep.setOrderNum(START_STEP);
        startStep.setCreateBy(flowDef.getCreateBy());
        startStep.setCreateTime(DateUtils.getNowDate());
        flowStepDefMapper.insertFlowStepDef(startStep);

        FlowStepDef approveStep = new FlowStepDef();
        approveStep.setFlowId(flowDef.getId());
        approveStep.setStepCode(APPROVE_STEP_CODE);
        approveStep.setStepName(APPROVE_STEP_NAME);
        approveStep.setNextOnPass(CLOSE_STEP_CODE);
        approveStep.setNextOnReject(CLOSE_STEP_CODE);
        approveStep.setOrderNum(APPROVE_STEP);
        approveStep.setCreateBy(flowDef.getCreateBy());
        approveStep.setCreateTime(DateUtils.getNowDate());
        flowStepDefMapper.insertFlowStepDef(approveStep);

        FlowStepDef closeStep = new FlowStepDef();
        closeStep.setFlowId(flowDef.getId());
        closeStep.setStepCode(CLOSE_STEP_CODE);
        closeStep.setStepName(CLOSE_STEP_NAME);
        closeStep.setOrderNum(CLOSE_STEP);
        closeStep.setCreateBy(flowDef.getCreateBy());
        closeStep.setCreateTime(DateUtils.getNowDate());
        flowStepDefMapper.insertFlowStepDef(closeStep);
        return rows;
    }


    public int updateFlowDef(FlowDef flowDef) {
        FlowDef exist = flowDefMapper.selectFlowDefById(flowDef.getId());
        if (exist == null) {
            throw new RuntimeException("流程定义不存在");
        }
        // 如果已发布，不允许修改
        if (exist.isPublish()) {
            throw new RuntimeException("已发布的流程不允许修改，请进行版本迭代");
        }
        flowDef.setFlowCode(exist.getFlowCode());
        flowDef.setVersion(exist.getVersion());
        flowDef.setPublish(exist.isPublish());
        flowDef.setDel(exist.isDel());
        flowDef.setUpdateTime(DateUtils.getNowDate());
        return flowDefMapper.updateFlowDef(flowDef);
    }


    public int deleteFlowDefByIds(Long[] ids) {
        return flowDefMapper.deleteFlowDefByIds(ids);
    }


    public int deleteFlowDefById(Long id) {
        return flowDefMapper.deleteFlowDefById(id);
    }

    /**
     * 发布流程
     *
     * @param id 流程定义ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public int publishFlowDef(Long id) {
        FlowDef flowDef = flowDefMapper.selectFlowDefById(id);
        if (flowDef == null) {
            throw new RuntimeException("流程定义不存在");
        }
        if (flowDef.isPublish()) {
            throw new RuntimeException("流程已发布，无需重复发布");
        }
        flowDef.setPublish(true);
        flowDef.setUpdateTime(DateUtils.getNowDate());
        return flowDefMapper.updateFlowDef(flowDef);
    }

    /**
     * 版本迭代：复制FlowDef及其step，flowCode不变version+1，step除了flowId改变其余不变
     *
     * @param id       当前流程定义ID
     * @param createBy 创建人
     * @return 新版本流程定义ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long iterateFlowDefVersion(Long id, String createBy) {
        FlowDef sourceFlowDef = flowDefMapper.selectFlowDefById(id);
        if (sourceFlowDef == null) {
            throw new RuntimeException("流程定义不存在");
        }

        // 创建新版本的FlowDef
        FlowDef newFlowDef = new FlowDef();
        newFlowDef.setFlowCode(sourceFlowDef.getFlowCode());
        newFlowDef.setFlowName(sourceFlowDef.getFlowName());
        newFlowDef.setFormSchema(sourceFlowDef.getFormSchema());
        newFlowDef.setVersion((sourceFlowDef.getVersion() == null ? 1 : sourceFlowDef.getVersion()) + 1);
        newFlowDef.setPublish(false);
        newFlowDef.setDel(false);
        newFlowDef.setRemark(sourceFlowDef.getRemark());
        newFlowDef.setCreateBy(createBy);
        newFlowDef.setCreateTime(DateUtils.getNowDate());
        flowDefMapper.insertFlowDef(newFlowDef);

        // 复制所有步骤
        FlowStepDef queryStep = new FlowStepDef();
        queryStep.setFlowId(id);
        List<FlowStepDef> sourceSteps = flowStepDefMapper.selectFlowStepDefList(queryStep);
        for (FlowStepDef sourceStep : sourceSteps) {
            FlowStepDef newStep = new FlowStepDef();
            newStep.setFlowId(newFlowDef.getId());
            newStep.setStepCode(sourceStep.getStepCode());
            newStep.setStepName(sourceStep.getStepName());
            newStep.setNextOnPass(sourceStep.getNextOnPass());
            newStep.setNextOnReject(sourceStep.getNextOnReject());
            newStep.setHandlerType(sourceStep.getHandlerType());
            newStep.setHandlerValue(sourceStep.getHandlerValue());
            newStep.setEventOnPass(sourceStep.getEventOnPass());
            newStep.setEventOnReject(sourceStep.getEventOnReject());
            newStep.setOrderNum(sourceStep.getOrderNum());
            newStep.setRemark(sourceStep.getRemark());
            newStep.setCreateBy(createBy);
            newStep.setCreateTime(DateUtils.getNowDate());
            flowStepDefMapper.insertFlowStepDef(newStep);
        }

        return newFlowDef.getId();
    }

}

