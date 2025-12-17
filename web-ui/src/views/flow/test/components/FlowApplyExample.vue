<template>
  <el-card shadow="never">
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 600;">发起流程组件示例</span>
        <el-button type="primary" icon="Plus" :disabled="!selectedFlowId" @click="openApply">发起流程</el-button>
      </div>
    </template>

    <el-form :inline="true" class="mb10">
      <el-form-item label="选择流程">
        <el-select v-model="selectedFlowId" placeholder="请选择流程" filterable style="width: 240px">
          <el-option v-for="item in flowOptions" :key="item.id" :label="item.flowName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="项目名称">
        <el-input v-model="initialFormData.projectName" placeholder="请输入项目名称" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button icon="Refresh" @click="loadFlows">刷新流程</el-button>
      </el-form-item>
    </el-form>

    <!-- 代码示例 -->
    <el-collapse v-model="activeCodeTab" class="code-example">
      <el-collapse-item name="code" title="📝 代码示例">
        <pre><code>{{ codeExample }}</code></pre>
      </el-collapse-item>
    </el-collapse>

    <!-- 申请组件 -->
    <FlowApplyDialog
      v-model="applyVisible"
      :flow-id="selectedFlowId"
      :ref-id="projectId"
      ref-module="演示模块"
      :instance-id="editingInstanceId"
      :initial-form-data="initialFormData"
      @saved="handleSaved"
      @submitted="handleSubmitted"
    />
  </el-card>
</template>

<script setup name="FlowApplyExample">
import { ref, onMounted } from "vue"
import { listFlowDef } from "@/api/flow/definition.js"
import FlowApplyDialog from "@/views/flow/flowApplyDialog/index.vue"
import { ElMessage } from "element-plus"


const flowOptions = ref([])
const selectedFlowId = ref()
const applyVisible = ref(false)
const editingInstanceId = ref(null)
const activeCodeTab = ref([])

// 初始表单数据，可以从外部传入（如项目ID等）
const initialFormData = ref({
  projectName: "项目1"
})
const projectId=ref(111)

const codeExample = ref(`<template>
  <div>
    <!-- 发起流程按钮 -->
    <el-button type="primary" @click="openApply">发起流程</el-button>

    <!-- 申请组件 -->
    <FlowApplyDialog
      v-model="applyVisible"
      :flow-id="selectedFlowId"             新增流程id 与实例id二选一必填
      :instance-id="editingInstanceId"      编辑实例id
      :initial-form-data="initialFormData"  初始化表单，如项目名称，可为空
      :ref-id="projectId"                   引用的业务表ID，一般都会有
      :ref-module="name"                    模块名可不填
      @saved="handleSaved"                  主键暂存后的回调，可不填
      @submitted="handleSubmitted"          组件提交后的回调，如getList刷新
    />
  </div>
</template>

<script setup>
import { ref } from "vue"
import FlowApplyDialog from "@/views/flow/flowApplyDialog"

const applyVisible = ref(false)
const selectedFlowId = ref(1) // 流程ID
const editingInstanceId = ref(null) // 编辑时传入实例ID，新建时传null

// 初始表单数据（可选），会合并到表单数据中
const initialFormData = ref({
  projectId: "PROJECT-001",
  projectName: "项目1"
})

function openApply() {
  editingInstanceId.value = null // 新建时设为null
  applyVisible.value = true
}

function handleSaved() {
  // 暂存成功后的回调
  console.log("暂存成功")
}

function handleSubmitted() {
  // 提交成功后的回调
  console.log("提交成功")
}
<\/script>`)

onMounted(() => {
  loadFlows()
})

function loadFlows() {
  listFlowDef({ pageNum: 1, pageSize: 200 }).then(res => {
    flowOptions.value = res.rows || []
  })
}

function openApply() {
  editingInstanceId.value = null
  applyVisible.value = true
}

function handleSaved() {
  ElMessage.success("暂存成功")
}

function handleSubmitted() {
  ElMessage.success("提交成功")
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
