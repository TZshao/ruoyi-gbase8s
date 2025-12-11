<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="流程编号" prop="flowCode">
        <el-input v-model="queryParams.flowCode" placeholder="请输入流程编号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="流程名称" prop="flowName">
        <el-input v-model="queryParams.flowName" placeholder="请输入流程名称" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flow:def:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['flow:def:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['flow:def:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="流程名称" align="center" prop="flowName" />
      <el-table-column label="版本" align="center" prop="version" width="80" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="300">
        <template #default="scope">
          <el-button link type="primary" icon="Setting" @click="openConfigStep(scope.row)" v-hasPermi="['flow:step:list']">配置流程</el-button>
        </template>
      </el-table-column>
         <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="300">
        <template #default="scope">
          <el-button link type="primary" icon="Document" @click="openConfigForm(scope.row)" v-hasPermi="['flow:def:edit']">配置表单结构</el-button>
        </template>
      </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="300">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="openViewInstance(scope.row)" v-hasPermi="['flow:instance:list']">查看实例</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body align-center destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="流程名称" prop="flowName">
              <el-input v-model="form.flowName" placeholder="请输入流程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="流程表单" prop="formSchema">
              <el-input v-model="form.formSchema" type="textarea" :rows="6" placeholder="请通过“配置表单结构”维护，保存后自动填充" readonly disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 配置流程步骤弹窗 -->
    <el-dialog title="配置流程" v-model="stepDialogOpen" width="1000px" append-to-body align-center destroy-on-close>
      <div class="mb10">
        <el-button type="primary" plain icon="Plus" @click="handleAddStep" v-hasPermi="['flow:step:add']">新增步骤</el-button>
      </div>
      <el-table :data="stepList" v-loading="stepLoading" border height="420px">
        <el-table-column label="顺序" prop="orderNum" width="80" />
        <el-table-column label="步骤编码" prop="stepCode" width="150" />
        <el-table-column label="步骤名称" prop="stepName" />
        <el-table-column label="通过后" prop="nextOnPass" width="140" />
        <el-table-column label="拒绝后" prop="nextOnReject" width="140" />
        <el-table-column label="处理人类型" prop="handlerType" width="120" />
        <el-table-column label="处理人值" prop="handlerValue" />
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEditStep(scope.row)" v-hasPermi="['flow:step:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDeleteStep(scope.row)" v-hasPermi="['flow:step:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="stepDialogOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 步骤编辑弹窗 -->
    <el-dialog :title="stepTitle" v-model="stepFormOpen" width="1000px" append-to-body>
      <el-form ref="stepFormRef" :model="stepForm" :rules="stepRules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="步骤编码" prop="stepCode">
              <el-input v-model="stepForm.stepCode" placeholder="请输入步骤编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="步骤名称" prop="stepName">
              <el-input v-model="stepForm.stepName" placeholder="请输入步骤名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通过后跳转" prop="nextOnPass">
              <el-input v-model="stepForm.nextOnPass" placeholder="FINISH/START/其他步骤编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拒绝后跳转" prop="nextOnReject">
              <el-input v-model="stepForm.nextOnReject" placeholder="START 或 下一步骤编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审批人类型" prop="handlerType">
              <el-input v-model="stepForm.handlerType" placeholder="角色/用户/部门等" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审批人取值" prop="handlerValue">
              <el-input v-model="stepForm.handlerValue" placeholder="ROLE_ADMIN / 1001 等" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通过事件" prop="eventOnPass">
              <el-input v-model="stepForm.eventOnPass" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拒绝事件" prop="eventOnReject">
              <el-input v-model="stepForm.eventOnReject" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="orderNum">
              <el-input-number v-model="stepForm.orderNum" :min="0" :max="999" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitStepForm">确 定</el-button>
          <el-button @click="stepFormOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 配置表单结构弹窗 -->
    <el-dialog title="配置表单结构" v-model="formSchemaDialogOpen" width="1000px" append-to-body align-center destroy-on-close>
      <el-form ref="formSchemaRef" :model="formSchemaForm" label-width="120px">
        <el-alert type="info" :closable="false" class="mb10">
          <template #title>字段仅在允许的步骤号内可编辑。</template>
        </el-alert>
        <el-table :data="formSchemaForm.fields" border style="width: 100%" height="420px">
          <el-table-column label="启用步骤编码" width="180">
            <template #default="scope">
              <el-input v-model="scope.row.enableIn" placeholder="步骤编码，多个用逗号分隔" />
            </template>
          </el-table-column>
          <el-table-column label="字段名" width="150">
            <template #default="scope">
              <el-input v-model="scope.row.name" placeholder="字段名" />
            </template>
          </el-table-column>
          <el-table-column label="字段标签" width="150">
            <template #default="scope">
              <el-input v-model="scope.row.label" placeholder="字段标签" />
            </template>
          </el-table-column>
          <el-table-column label="字段类型" width="150">
            <template #default="scope">
              <el-select v-model="scope.row.type" placeholder="请选择" style="width: 100%">
                <el-option label="下拉(select)" value="select" />
                <el-option label="日期(date)" value="date" />
                <el-option label="数字(number)" value="number" />
                <el-option label="字符串(String)" value="string" />
                <el-option label="文本(text)" value="text" />
                <el-option label="文件(file)" value="file" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="可见性" width="130" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.visible"
                :active-value="1"
                :inactive-value="0"
              />
            </template>
          </el-table-column>
          <el-table-column label="必填" width="100" align="center">
            <template #default="scope">
              <el-switch v-model="scope.row.required" :active-value="true" :inactive-value="true"/>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button link type="danger" icon="Delete" @click="removeField(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="mt10">
          <el-button type="primary" plain icon="Plus" @click="addField">添加字段</el-button>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFormSchema">确 定</el-button>
          <el-button @click="formSchemaDialogOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看实例弹窗 -->
    <el-dialog title="查看实例" v-model="instanceDialogOpen" width="1200px" append-to-body align-center destroy-on-close>
      <el-form :model="instanceQueryParams" ref="instanceQueryRef" :inline="true" class="mb10">
        <el-form-item label="状态" prop="status">
          <el-select v-model="instanceQueryParams.status" clearable placeholder="请选择状态" style="width: 180px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleInstanceQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetInstanceQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="instanceList" v-loading="instanceLoading" border>
        <el-table-column label="实例ID" prop="id" width="90" />
        <el-table-column label="状态" prop="status" width="110">
          <template #default="scope">
            <dict-tag :options="statusOptions" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="当前步骤" prop="currentStepCode" width="160" />
        <el-table-column label="申请人ID" prop="applicantId" width="120" />
        <el-table-column label="表单数据" min-width="260">
          <template #default="scope">
            <span class="json-preview">{{ formatFormData(scope.row.formData) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="instanceTotal > 0" :total="instanceTotal" v-model:page="instanceQueryParams.pageNum" v-model:limit="instanceQueryParams.pageSize" @pagination="getInstanceList" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="instanceDialogOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowDefinition">
import { listFlowDef, getFlowDef, addFlowDef, updateFlowDef, delFlowDef } from "@/api/flow/definition"
import { listFlowStep, getFlowStep, addFlowStep, updateFlowStep, delFlowStep } from "@/api/flow/step"
import { listInstance } from "@/api/flow/instance"
import { useRouter } from "vue-router"

const router = useRouter()
const { proxy } = getCurrentInstance()

const dataList = ref([])
const loading = ref(false)
const showSearch = ref(true)
const open = ref(false)
const total = ref(0)
const title = ref("")
const ids = ref([])
const single = ref(true)
const multiple = ref(true)

// 配置流程步骤相关
const stepDialogOpen = ref(false)
const stepFormOpen = ref(false)
const stepTitle = ref("")
const stepList = ref([])
const stepLoading = ref(false)
const currentFlowId = ref(null)
const stepForm = reactive({
  id: undefined,
  flowId: null,
  stepCode: "",
  stepName: "",
  nextOnPass: "",
  nextOnReject: "",
  handlerType: "",
  handlerValue: "",
  eventOnPass: "",
  eventOnReject: "",
  orderNum: 0
})
const stepRules = {
  stepCode: [{ required: true, message: "步骤编码不能为空", trigger: "blur" }],
  stepName: [{ required: true, message: "步骤名称不能为空", trigger: "blur" }]
}

// 配置表单结构相关
const formSchemaDialogOpen = ref(false)
const formSchemaForm = reactive({
  flowId: null,
  fields: []
})

function normalizeField(field = {}) {
  return {
    enableIn: "",
    name: "",
    label: "",
    type: "string",
    required: true,
    visible: 1,
    ...field
  }
}

// 查看实例相关
const instanceDialogOpen = ref(false)
const instanceList = ref([])
const instanceLoading = ref(false)
const instanceTotal = ref(0)
const instanceQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  flowId: null,
  status: undefined
})
const statusOptions = [
  { label: "运行中", value: "RUNNING" },
  { label: "通过", value: "PASS" },
  { label: "驳回", value: "REJECT" },
  { label: "取消", value: "CANCEL" }
]

function formatFormData(data) {
  if (!data) return "-"
  try {
    const parsed = typeof data === "string" ? JSON.parse(data) : data
    const str = JSON.stringify(parsed)
    return str.length > 80 ? `${str.slice(0, 80)}...` : str
  } catch (e) {
    return String(data)
  }
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    flowCode: undefined,
    flowName: undefined
  },
  rules: {
    flowName: [{ required: true, message: "流程名称不能为空", trigger: "blur" }]
  }
})

const { form, queryParams, rules } = toRefs(data)

function getList() {
  loading.value = true
  listFlowDef(queryParams.value).then(res => {
    dataList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    id: undefined,
    flowCode: "",
    flowName: "",
    version: 1,
    formSchema: "",
    remark: ""
  }
  proxy.resetForm("formRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增流程"
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getFlowDef(id).then(res => {
    form.value = res.data
    open.value = true
    title.value = "修改流程"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    if (form.value.id) {
      updateFlowDef(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      addFlowDef(form.value).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const delIds = row.id || ids.value
  proxy.$modal.confirm("确认删除选中的数据吗？").then(() => delFlowDef(delIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function cancel() {
  open.value = false
  reset()
}

function openStep(row) {
  router.push({ path: "/flow/step", query: { flowId: row.id, flowName: row.flowName } })
}

// 配置流程步骤
function openConfigStep(row) {
  currentFlowId.value = row.id
  stepDialogOpen.value = true
  getStepList()
}

function getStepList() {
  stepLoading.value = true
  listFlowStep({ flowId: currentFlowId.value, pageNum: 1, pageSize: 100 }).then(res => {
    stepList.value = res.rows || []
    stepLoading.value = false
  })
}

function handleAddStep() {
  stepForm.id = undefined
  stepForm.flowId = currentFlowId.value
  stepForm.stepCode = ""
  stepForm.stepName = ""
  stepForm.nextOnPass = ""
  stepForm.nextOnReject = ""
  stepForm.handlerType = ""
  stepForm.handlerValue = ""
  stepForm.eventOnPass = ""
  stepForm.eventOnReject = ""
  stepForm.orderNum = 0
  stepTitle.value = "新增步骤"
  stepFormOpen.value = true
}

function handleEditStep(row) {
  getFlowStep(row.id).then(res => {
    Object.assign(stepForm, res.data)
    stepTitle.value = "修改步骤"
    stepFormOpen.value = true
  })
}

function submitStepForm() {
  proxy.$refs["stepFormRef"].validate(valid => {
    if (!valid) return
    if (stepForm.id) {
      updateFlowStep(stepForm).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        stepFormOpen.value = false
        getStepList()
      })
    } else {
      addFlowStep(stepForm).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        stepFormOpen.value = false
        getStepList()
      })
    }
  })
}

function handleDeleteStep(row) {
  proxy.$modal.confirm("确认删除该步骤吗？").then(() => delFlowStep(row.id)).then(() => {
    getStepList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 配置表单结构
function openConfigForm(row) {
  formSchemaForm.flowId = row.id
  try {
    const schema = row.formSchema ? JSON.parse(row.formSchema) : { fields: [] }
    formSchemaForm.fields = (schema.fields || []).map(item => normalizeField(item))
  } catch (e) {
    formSchemaForm.fields = []
  }
  formSchemaDialogOpen.value = true
}

function addField() {
  formSchemaForm.fields.push(normalizeField())
}

function removeField(index) {
  formSchemaForm.fields.splice(index, 1)
}

function submitFormSchema() {
  const schema = { fields: formSchemaForm.fields.map(item => normalizeField(item)) }
  getFlowDef(formSchemaForm.flowId).then(res => {
    const flowDef = res.data
    flowDef.formSchema = JSON.stringify(schema)
    updateFlowDef(flowDef).then(() => {
      proxy.$modal.msgSuccess("保存成功")
      formSchemaDialogOpen.value = false
      getList()
    })
  })
}

// 查看实例
function openViewInstance(row) {
  instanceQueryParams.flowId = row.id
  instanceQueryParams.pageNum = 1
  instanceDialogOpen.value = true
  getInstanceList()
}

function getInstanceList() {
  instanceLoading.value = true
  listInstance(instanceQueryParams).then(res => {
    instanceList.value = res.rows || []
    instanceTotal.value = res.total || 0
    instanceLoading.value = false
  })
}

function handleInstanceQuery() {
  instanceQueryParams.pageNum = 1
  getInstanceList()
}

function resetInstanceQuery() {
  instanceQueryParams.status = undefined
  handleInstanceQuery()
}

getList()
</script>

<style scoped>
.json-preview {
  display: inline-block;
  max-width: 100%;
  font-family: monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
