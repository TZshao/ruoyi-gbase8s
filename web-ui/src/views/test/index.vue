<template>
  <div class="app-container">
<!--    <el-alert-->
<!--      type="info"-->
<!--      :closable="false"-->
<!--      class="mb20"-->
<!--    >-->
<!--      <template #title>-->
<!--        <div>-->
<!--          <strong>模块测试</strong>-->
<!--        </div>-->
<!--      </template>-->
<!--    </el-alert>-->

    <el-tabs v-model="activeTab" class="test-tabs">
      <el-tab-pane label="测试流程模块" name="flow">
        <el-row :gutter="20" class="component-row">
          <el-col :span="12">
            <div >
              <FlowApplyExample />
            </div>
          </el-col>
          <el-col :span="12">
            <div >
              <FlowApproveExample />
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="权限SQL" name="auth">
        <el-row :gutter="20">
          <el-col :span="24">
            <div  style="max-height: 600px !important;">
              <TestAuthExample />
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="SQL Server数据源" name="sqlserver">
        <SqlServerDataSourceExample />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="FlowTest">
import { ref } from "vue"
import FlowApplyExample from "./components/FlowApplyExample.vue"
import FlowApproveExample from "./components/FlowApproveExample.vue"
import TestAuthExample from "./components/TestAuthExample.vue"
import SqlServerDataSourceExample from "./components/SqlServerDataSourceExample.vue"

const activeTab = ref("flow")
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}

.test-tabs {
  margin-top: 10px;
}

.test-tabs :deep(.el-tabs__content) {
  padding-top: 20px;
}

.component-row {
  display: flex;
  align-items: stretch;
}

</style>
