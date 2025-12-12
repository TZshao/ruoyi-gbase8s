<template>
  <el-card shadow="never">
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 600;">审批流程组件示例</span>
        <el-button type="primary" icon="View" :disabled="!selectedInstanceId" @click="openApprove">审批</el-button>
      </div>
    </template>

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
      class="mb10"
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
      class="mb10"
    />

    <!-- 代码示例 -->
    <el-collapse v-model="activeCodeTab" class="code-example">
      <el-collapse-item name="code" title="📝 代码示例">
        <pre><code>{{ codeExample }}</code></pre>
      </el-collapse-item>
    </el-collapse>

    <!-- 审批组件 -->
    <FlowApproveDialog
      v-model="approveVisible"
      :instance-id="selectedInstanceId"
      @approved="handleApproved"
    />
  </el-card>
</template>

<script setup name="FlowApproveExample">
import { ref, reactive, onMounted } from "vue"
import { parseTime } from "@/utils/ruoyi.js"
import { listInstance } from "@/api/flow/instance.js"
import FlowApproveDialog from "@/views/flow/flowApproveDialog/index.vue"
import { ElMessage } from "element-plus"

const instanceQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined
})
const instanceList = ref([])
const instanceTotal = ref(0)
const instanceLoading = ref(false)
const selectedInstanceId = ref()
const approveVisible = ref(false)
const activeCodeTab = ref([])

const statusOptions = [
  { label: "待提交", value: "PENDING", elTagType: "primary" },
  { label: "运行中", value: "RUNNING", elTagType: "success" },
  { label: "已关闭", value: "CLOSED", elTagType: "info" }
]

const codeExample = ref(`<template>
  <div>
    <!-- 审批按钮 -->
    <el-button type="primary" @click="openApprove">审批</el-button>

    <!-- 审批组件 -->
    <FlowApproveDialog
      v-model="approveVisible"
      :instance-id="instanceId"
      @approved="handleApproved"
    />
  </div>
</template>

<script setup>
import { ref } from "vue"
import FlowApproveDialog from "@/views/flow/flowApproveDialog"

const approveVisible = ref(false)
const instanceId = ref(1) // 流程实例ID

function openApprove() {
  approveVisible.value = true
}

function handleApproved() {
  // 审批成功后的回调
  console.log("审批成功")
  // 可以在这里刷新列表等操作
}
<\/script>`)

onMounted(() => {
  getInstanceList()
})

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

function openApprove() {
  if (!selectedInstanceId.value) {
    ElMessage.warning("请先选择一条记录")
    return
  }
  approveVisible.value = true
}

function handleApproved() {
  ElMessage.success("审批成功")
  getInstanceList()
}
</script>

<style scoped>
.code-example {
  margin-top: 20px;
}
.code-example pre {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 0;
}
.code-example code {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #333;
}
.mb10 {
  margin-bottom: 10px;
}
</style>
