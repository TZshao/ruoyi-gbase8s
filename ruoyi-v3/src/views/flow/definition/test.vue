<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card shadow="never">
          <div class="card-header">发起流程</div>
          <el-form :inline="true">
            <el-form-item label="选择流程">
              <el-select v-model="selectedFlowId" placeholder="请选择流程" filterable style="width: 240px">
                <el-option v-for="item in flowOptions" :key="item.id" :label="item.flowName" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Plus" :disabled="!selectedFlowId" @click="openApply">发起</el-button>
              <el-button icon="Refresh" @click="loadFlows">刷新流程</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never">
          <div class="card-header">实例审批</div>
          <el-form :inline="true" class="mb10">
            <el-form-item label="状态">
              <el-select v-model="instanceQuery.status" clearable placeholder="全部" style="width: 160px">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleInstanceQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetInstanceQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table
            v-loading="instanceLoading"
            :data="instanceList"
            highlight-current-row
            @current-change="handleInstanceSelect"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="flowName" label="流程" min-width="120" />
            <el-table-column prop="currentStepCode" label="当前步骤" width="120" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <dict-tag :options="statusOptions" :value="scope.row.status" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="instanceTotal > 0"
            :total="instanceTotal"
            v-model:page="instanceQuery.pageNum"
            v-model:limit="instanceQuery.pageSize"
            @pagination="getInstanceList"
          />
          <div class="mt10">
            <el-button type="primary" icon="View" :disabled="!selectedInstanceId" @click="openApprove">审批</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <FlowApplyDialog
      v-model="applyVisible"
      :flow-id="selectedFlowId"
      @saved="getInstanceList"
      @submitted="getInstanceList"
    />
    <FlowApproveDialog
      v-model="approveVisible"
      v-if="approveVisible"
      :instance-id="selectedInstanceId"
      @approved="getInstanceList"
    />
  </div>
</template>

<script setup name="FlowTest">
import { onMounted, reactive, ref } from "vue"
import { parseTime } from "@/utils/ruoyi"
import { listFlowDef } from "@/api/flow/definition"
import { listInstance } from "@/api/flow/instance"
import FlowApplyDialog from "@/components/FlowApplyDialog"
import FlowApproveDialog from "@/components/FlowApproveDialog"

const flowOptions = ref([])
const selectedFlowId = ref()

const instanceQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined
})
const instanceList = ref([])
const instanceTotal = ref(0)
const instanceLoading = ref(false)
const selectedInstanceId = ref()

const applyVisible = ref(false)
const approveVisible = ref(false)

const statusOptions = [
  { label: "运行中", value: "RUNNING" },
  { label: "通过", value: "PASS" },
  { label: "驳回", value: "REJECT" },
  { label: "取消", value: "CANCEL" }
]

onMounted(() => {
  loadFlows()
  getInstanceList()
})

function loadFlows() {
  listFlowDef({ pageNum: 1, pageSize: 200 }).then(res => {
    flowOptions.value = res.rows || []
  })
}

function getInstanceList() {
  instanceLoading.value = true
  listInstance(instanceQuery).then(res => {
    instanceList.value = res.rows || []
    instanceTotal.value = res.total || 0
    instanceLoading.value = false
  })
}

function handleInstanceQuery() {
  instanceQuery.pageNum = 1
  getInstanceList()
}

function resetInstanceQuery() {
  instanceQuery.status = undefined
  handleInstanceQuery()
}

function handleInstanceSelect(row) {
  selectedInstanceId.value = row?.id
}

function openApply() {
  applyVisible.value = true
}

function openApprove() {
  if (!selectedInstanceId.value) return
  approveVisible.value = true
}
</script>

<style scoped>
.card-header {
  font-weight: 600;
  margin-bottom: 10px;
}
.mb10 {
  margin-bottom: 10px;
}
.mt10 {
  margin-top: 10px;
}
</style>
