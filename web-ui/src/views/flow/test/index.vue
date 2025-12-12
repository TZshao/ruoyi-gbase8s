<template>
  <div class="app-container">
    <el-alert
      type="info"
      :closable="false"
      class="mb20"
    >
      <template #title>
        <div>
          <strong>组件使用说明</strong>
          <p style="margin: 8px 0 0 0; font-size: 13px;">
            本页面展示了流程申请组件和审批组件的使用示例。开发者可以参考下方代码示例，在自己的页面中快速集成流程功能。
          </p>
        </div>
      </template>
    </el-alert>

    <el-row :gutter="20">
      <el-col :span="12">
        <FlowApplyExample />
      </el-col>
      <el-col :span="12">
        <FlowApproveExample />
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="FlowTest">
import FlowApplyExample from "./components/FlowApplyExample.vue"
import FlowApproveExample from "./components/FlowApproveExample.vue"
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}
</style>
