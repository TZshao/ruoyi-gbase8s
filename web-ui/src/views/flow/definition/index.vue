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
        <el-button type="warning" plain icon="CopyDocument" :disabled="single" @click="handleIterateFromList" v-hasPermi="['flow:def:add']">版本迭代</el-button>
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
      <el-table-column label="发布状态" align="center" prop="publish" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.publish ? 'success' : 'info'">
            {{ scope.row.publish ? '已发布' : '未发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="配置" align="center" class-name="small-padding fixed-width" width="300">
        <template #default="scope">
          <el-button link type="primary" icon="Setting" @click="openConfigStep(scope.row)" v-hasPermi="['flow:step:list']" :disabled="scope.row.publish">配置流程</el-button>
          <el-button link type="primary" icon="Document" @click="openConfigForm(scope.row)" v-hasPermi="['flow:def:edit']" :disabled="scope.row.publish">配置表单结构</el-button>
        </template>
      </el-table-column>
      <el-table-column label="动作" align="center" class-name="small-padding fixed-width" width="300">
        <template #default="scope">
          <el-button link type="success" icon="Promotion" @click="handlePublish(scope.row)" v-hasPermi="['flow:def:edit']" :disabled="scope.row.publish">发布</el-button>
              <el-button link type="warning" icon="CopyDocument" @click="openIterateDialog(scope.row)" v-hasPermi="['flow:def:add']">版本迭代</el-button>
        </template>
      </el-table-column>
      <el-table-column label="查看" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="openViewInstanceByCode(scope.row)" v-hasPermi="['flow:instance:list']">查看实例</el-button>
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
          <el-col :span="12">
            <el-form-item label="流程代码" prop="flowCode">
              <el-input v-model="form.flowCode" placeholder="请输入流程代码" />
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
    <el-dialog title="配置流程" v-model="stepDialogOpen" width="1400px" append-to-body align-center destroy-on-close>
      <div class="mb10">
        <el-button type="primary" plain icon="Plus" @click="handleAddStep" v-hasPermi="['flow:step:add']">新增步骤</el-button>
      </div>
      <el-table :data="stepList" v-loading="stepLoading" border height="420px">
        <el-table-column label="顺序" prop="orderNum" width="80" />
        <el-table-column label="名称">
          <template #default="scope">
            {{scope.row.stepName}}
          </template>
        </el-table-column>
        <el-table-column label="通过" prop="nextOnPass" width="140">
          <template #default="scope">
            {{ getStepNameByCode(scope.row.nextOnPass) || scope.row.nextOnPass || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="通过事件" prop="eventOnPass" width="140"/>
        <el-table-column label="拒绝" prop="nextOnReject" width="140">
          <template #default="scope">
            {{ getStepNameByCode(scope.row.nextOnReject) || scope.row.nextOnReject || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="拒绝事件" prop="eventOnReject" width="140"/>
        <el-table-column label="审批类型" prop="handlerType" width="120" />
        <el-table-column label="处理人值" prop="handlerValue" />
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button link type="primary" :disabled="scope.row.stepCode==='CLOSED'" icon="Edit" @click="handleEditStep(scope.row)" v-hasPermi="['flow:step:edit']">修改</el-button>
            <el-button link type="danger" :disabled="scope.row.stepCode==='CLOSED'" icon="Delete" @click="handleDeleteStep(scope.row)" v-hasPermi="['flow:step:remove']">删除</el-button>
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
            <el-form-item label="步骤名称" prop="stepName">
              <el-input v-model="stepForm.stepName" placeholder="中文名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="步骤编码" prop="stepCode">
              <el-input v-model="stepForm.stepCode" placeholder="英文字符" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通过后步骤" prop="nextOnPass">
              <el-select v-model="stepForm.nextOnPass" placeholder="请选择" filterable style="width: 100%">
                <el-option v-for="item in stepOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拒绝后步骤" prop="nextOnReject">
              <el-select v-model="stepForm.nextOnReject" placeholder="请选择" filterable style="width: 100%">
                <el-option v-for="item in stepOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审批类型" prop="handlerType">
              <el-input v-model="stepForm.handlerType" placeholder="等权限控制完善后实现" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审批人" prop="handlerValue">
              <el-input v-model="stepForm.handlerValue" placeholder="等权限控制完善后实现" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通过时触发" prop="eventOnPass">
              <el-row :gutter="10">
                <el-col :span="11">
                  <el-select v-model="eventPass.cls" placeholder="类 + 方法" filterable clearable style="width: 100%">
                    <el-option v-for="item in triggerClassOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-col>
                <el-col :span="13">
                  <el-select v-model="eventPass.method" placeholder="默认静默流转到下一步" filterable clearable style="width: 100%">
                    <el-option v-for="item in triggerMethodOptions(eventPass.cls)" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-col>
              </el-row>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拒绝时触发" prop="eventOnReject">
              <el-row :gutter="10">
                <el-col :span="11">
                  <el-select v-model="eventReject.cls" placeholder="类 + 方法" filterable clearable style="width: 100%">
                    <el-option v-for="item in triggerClassOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-col>
                <el-col :span="13">
                  <el-select v-model="eventReject.method" placeholder="默认静默流转到下一步" filterable clearable style="width: 100%">
                    <el-option v-for="item in triggerMethodOptions(eventReject.cls)" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-col>
              </el-row>
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
    <el-dialog title="配置表单结构" v-model="formSchemaDialogOpen" width="1180x" append-to-body align-center destroy-on-close>
      <el-form ref="formSchemaRef" :model="formSchemaForm" label-width="120px">
        <el-tabs v-model="formSchemaActiveTab">
          <el-tab-pane label="字段表单" name="fields">
            <el-alert type="info" :closable="false" class="mb10">
              <template #title>字段仅在允许的步骤号内可编辑。</template>
            </el-alert>
            <el-alert
              v-if="isCustomPageEnabled"
              class="mb10"
              type="warning"
              :closable="false"
              title="已启用自定义页面，字段列表将不会生效。"
            />
            <el-table :data="formSchemaForm.fields" border style="width: 100%" height="420px">
              <el-table-column label="启用步骤" width="180">
                <template #default="scope">
                  <el-select v-model="scope.row.enableIn" placeholder="请选择步骤" multiple filterable collapse-tags collapse-tags-tooltip style="width: 100%" :disabled="isCustomPageEnabled">
                    <el-option v-for="item in formStepOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="字段标签" width="150">
                <template #default="scope">
                  <el-input v-model="scope.row.label" placeholder="字段标签" :disabled="isCustomPageEnabled" />
                </template>
              </el-table-column>
              <el-table-column label="字段名" width="150">
                <template #default="scope">
                  <el-input v-model="scope.row.name" placeholder="字段名" :disabled="isCustomPageEnabled" />
                </template>
              </el-table-column>
              <el-table-column label="字段类型" width="150">
                <template #default="scope">
                  <el-select v-model="scope.row.type" placeholder="请选择" style="width: 100%" :disabled="isCustomPageEnabled" @change="handleTypeChange(scope.row)">
                    <el-option label="下拉" value="select" />
                    <el-option label="日期" value="date" />
                    <el-option label="数字" value="number" />
                    <el-option label="字符串" value="string" />
                    <el-option label="文本" value="text" />
                    <el-option label="文件" value="file" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="字典类型" width="180" v-if="hasSelectField">
                <template #default="scope">
                  <el-select 
                    v-if="scope.row.type === 'select'"
                    v-model="scope.row.dictType" 
                    placeholder="请选择字典类型" 
                    style="width: 100%" 
                    filterable
                    clearable
                    :disabled="isCustomPageEnabled"
                  >
                    <el-option 
                      v-for="item in dictTypeOptions" 
                      :key="item.dictType" 
                      :label="item.dictName" 
                      :value="item.dictType" 
                    />
                  </el-select>
                  <span v-else style="color: #909399;">-</span>
                </template>
              </el-table-column>
              <el-table-column label="必填" width="100" align="center">
                <template #default="scope">
                  <el-switch v-model="scope.row.required" :active-value="true" :inactive-value="false" :disabled="isCustomPageEnabled"/>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="scope">
                  <el-button link type="danger" icon="Delete" @click="removeField(scope.$index)" :disabled="isCustomPageEnabled">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="mt10">
              <el-button type="primary" plain icon="Plus" @click="addField" :disabled="isCustomPageEnabled">添加字段</el-button>
            </div>
          </el-tab-pane>
          <el-tab-pane label="自定义页面" name="custom">
            <el-form-item label="启用自定义页">
              <el-switch v-model="formSchemaForm.customPage.enabled" :active-value="true" :inactive-value="false" />
            </el-form-item>
            <el-form-item label="组件路径" prop="customPage.component">
              <el-input v-model="formSchemaForm.customPage.component" placeholder="例如：@/views/flow/customizeFromTemplate.vue" clearable />
            </el-form-item>
            <el-alert
              type="info"
              :closable="false"
              title="启用后，表单将直接渲染此组件，不再生成字段。"
            />
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFormSchema">确 定</el-button>
          <el-button @click="formSchemaDialogOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 版本迭代弹窗 -->
    <el-dialog title="版本迭代" v-model="iterateDialogOpen" width="1000px" append-to-body align-center destroy-on-close>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>版本迭代 - {{ currentFlowCode }}</span>
          <el-button type="primary" icon="CopyDocument" @click="handleIterateNewVersion" v-hasPermi="['flow:def:add']" :disabled="!hasLatestPublished">迭代新版</el-button>
        </div>
      </template>
      <el-table :data="versionList" v-loading="versionLoading" border>
        <el-table-column label="版本" prop="version" width="100" align="center" />
        <el-table-column label="发布状态" prop="publish" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.publish ? 'success' : 'info'">
              {{ scope.row.publish ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" align="center">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="400">
          <template #default="scope">
            <el-button link type="primary" icon="Setting" @click="openConfigStep(scope.row)" v-hasPermi="['flow:step:list']">查看流程</el-button>
            <el-button link type="primary" icon="View" @click="openViewInstanceByFlowId(scope.row)" v-hasPermi="['flow:instance:list']">查看实例</el-button>
            <el-button link type="primary" icon="Document" @click="openConfigForm(scope.row)" v-hasPermi="['flow:def:edit']">查看表单</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="iterateDialogOpen = false">关 闭</el-button>
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
        <el-table-column label="流程编号" prop="flowCode" width="150" />
        <el-table-column label="流程版本" prop="flowVersion" width="100" />
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
import { listFlowDef, getFlowDef, addFlowDef, updateFlowDef, delFlowDef, publishFlowDef, iterateFlowDef, listFlowDefByFlowCode } from "@/api/flow/definition"
import { listFlowStep, getFlowStep, addFlowStep, updateFlowStep, delFlowStep, listStepTriggers } from "@/api/flow/step"
import { listInstance } from "@/api/flow/instance"
import { optionselect } from "@/api/system/dict/type"
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
const triggerOptions = ref([])
const triggerClassOptions = computed(() =>
  (triggerOptions.value || []).map(item => ({ label: item.className, value: item.className }))
)
const triggerMethodOptions = (cls) => {
  const target = (triggerOptions.value || []).find(item => item.className === cls)
  return target ? target.methods.map(m => ({ label: m, value: m })) : []
}

const eventPass = reactive({ cls: "", method: "" })
const eventReject = reactive({ cls: "", method: "" })
const stepOptions = computed(() => {
  const opts = (stepList.value || []).map(item => ({
    label: `${item.stepName || ""} (${item.stepCode})`.trim(),
    value: item.stepCode
  }))
  return [...opts]
})

// 表单结构配置的步骤选项
const formStepOptions = computed(() => {
  return (formStepList.value || []).map(item => ({
    label: `${item.stepName || ""} (${item.stepCode})`.trim(),
    value: item.stepCode
  }))
})

// 配置表单结构相关
const formSchemaDialogOpen = ref(false)
const formSchemaActiveTab = ref("fields")
const formSchemaForm = reactive({
  flowId: null,
  fields: [],
  customPage: {
    enabled: false,
    component: ""
  }
})
const formStepList = ref([])
const dictTypeOptions = ref([])
const hasSelectField = computed(() => {
  return formSchemaForm.fields.some(field => field.type === 'select')
})

function normalizeField(field = {}) {
  // 处理enableIn字段：如果是字符串（逗号分隔），转换为数组；如果是数组，保持原样
  let enableIn = field.enableIn || ""
  if (typeof enableIn === "string" && enableIn.trim()) {
    enableIn = enableIn.split(",").map(s => s.trim()).filter(s => s)
  } else if (!Array.isArray(enableIn)) {
    enableIn = []
  }
  return {
    name: "",
    label: "",
    type: "string",
    required: true,
    dictType: "",
    ...field,
    enableIn: enableIn
  }
}

function normalizeCustomPage(custom = {}) {
  return {
    enabled: false,
    component: "",
    ...custom
  }
}
const isCustomPageEnabled = computed(() => !!formSchemaForm.customPage?.enabled)

// 版本迭代相关
const iterateDialogOpen = ref(false)
const versionList = ref([])
const versionLoading = ref(false)
const currentFlowCode = ref("")
const hasLatestPublished = ref(false)

// 查看实例相关
const instanceDialogOpen = ref(false)
const instanceList = ref([])
const instanceLoading = ref(false)
const instanceTotal = ref(0)
const instanceQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  flowId: null,
  flowCode: null,
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
    if (res.data.publish) {
      proxy.$modal.msgError("已发布的流程不允许修改，请进行版本迭代")
      return
    }
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
      }).catch(err => {
        proxy.$modal.msgError(err.msg || "修改失败")
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
  loadTriggerOptions()
}

function getStepList() {
  stepLoading.value = true
  listFlowStep({ flowId: currentFlowId.value, pageNum: 1, pageSize: 100 }).then(res => {
    stepList.value = res.rows || []
    stepLoading.value = false
  })
}

function loadTriggerOptions() {
  listStepTriggers().then(res => {
    const raw = res.data || {}
    triggerOptions.value = Object.keys(raw).map(cls => ({
      className: cls,
      methods: raw[cls] || []
    }))
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
  eventPass.cls = ""
  eventPass.method = ""
  eventReject.cls = ""
  eventReject.method = ""
  stepForm.orderNum = computeNextOrder()
  stepTitle.value = "新增步骤"
  stepFormOpen.value = true
}

function computeNextOrder() {
  if (!Array.isArray(stepList.value) || stepList.value.length === 0) {
    return 1
  }
  const maxOrder = Math.max(...stepList.value.map(item => {
    var num = Number(item.orderNum || 0);
    return num >= 99 ? 1 : num;
  }));
  return Number.isFinite(maxOrder) ? maxOrder + 1 : 1
}

function handleEditStep(row) {
  getFlowStep(row.id).then(res => {
    Object.assign(stepForm, res.data)
    const [pCls, pMethod] = (stepForm.eventOnPass || "").split(".")
    const [rCls, rMethod] = (stepForm.eventOnReject || "").split(".")
    eventPass.cls = pCls || ""
    eventPass.method = pMethod || ""
    eventReject.cls = rCls || ""
    eventReject.method = rMethod || ""
    stepTitle.value = "修改步骤"
    stepFormOpen.value = true
  })
}

function submitStepForm() {
  // 组合事件
  stepForm.eventOnPass = eventPass.cls && eventPass.method ? `${eventPass.cls}.${eventPass.method}` : ""
  stepForm.eventOnReject = eventReject.cls && eventReject.method ? `${eventReject.cls}.${eventReject.method}` : ""
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
  formSchemaActiveTab.value = "fields"
  try {
    const schema = row.formSchema ? JSON.parse(row.formSchema) : { fields: [], customPage: {} }
    formSchemaForm.fields = (schema.fields || []).map(item => normalizeField(item))
    formSchemaForm.customPage = normalizeCustomPage(schema.customPage || {})
  } catch (e) {
    formSchemaForm.fields = []
    formSchemaForm.customPage = normalizeCustomPage()
  }
  // 加载该流程的步骤列表
  loadFormStepList(row.id)
  // 加载字典类型列表
  loadDictTypeOptions()
  if (formSchemaForm.customPage.enabled) {
    formSchemaActiveTab.value = "custom"
  }
  formSchemaDialogOpen.value = true
}

// 加载字典类型列表
function loadDictTypeOptions() {
  optionselect().then(res => {
    dictTypeOptions.value = res.data || []
  }).catch(() => {
    dictTypeOptions.value = []
  })
}

// 处理字段类型变化
function handleTypeChange(field) {
  // 如果类型不是下拉，清除字典类型
  if (field.type !== 'select') {
    field.dictType = ""
  }
}

function loadFormStepList(flowId) {
  listFlowStep({ flowId: flowId, pageNum: 1, pageSize: 100 }).then(res => {
    formStepList.value = res.rows || []
  })
}

// 根据步骤码获取步骤名称
function getStepNameByCode(stepCode) {
  if (!stepCode) return ""
  const step = stepList.value.find(item => item.stepCode === stepCode)
  return step ? step.stepName : ""
}

function addField() {
  formSchemaForm.fields.push(normalizeField())
}

function removeField(index) {
  formSchemaForm.fields.splice(index, 1)
}

function submitFormSchema() {
  // 将enableIn数组转换为逗号分隔的字符串保存
  const normalizedCustom = normalizeCustomPage(formSchemaForm.customPage)
  const schema = {
    customPage: normalizedCustom,
    fields: normalizedCustom.enabled ? [] : formSchemaForm.fields.map(item => {
      const field = normalizeField(item)
      return {
        ...field,
        enableIn: Array.isArray(field.enableIn) ? field.enableIn.join(",") : (field.enableIn || "")
      }
    })
  }
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

// 版本迭代
function openIterateDialog(row) {
  currentFlowCode.value = row.flowCode
  iterateDialogOpen.value = true
  loadVersionList()
}

function loadVersionList() {
  versionLoading.value = true
  listFlowDefByFlowCode(currentFlowCode.value, false).then(res => {
    versionList.value = (res.data || []).sort((a, b) => (b.version || 0) - (a.version || 0))
    // 检查是否有最新已发布的版本
    hasLatestPublished.value = versionList.value.length > 0 && versionList.value[0].publish
    versionLoading.value = false
  }).catch(() => {
    versionLoading.value = false
  })
}

function handleIterateNewVersion() {
  if (versionList.value.length === 0) {
    proxy.$modal.msgError("没有可迭代的版本")
    return
  }
  const latestVersion = versionList.value[0]
  if (!latestVersion.publish) {
    proxy.$modal.msgError("最新版本未发布，无法迭代")
    return
  }
  proxy.$modal.confirm("确认进行版本迭代吗？将创建新版本，流程编号不变，版本号+1。").then(() => {
    iterateFlowDef(latestVersion.id).then(res => {
      proxy.$modal.msgSuccess("版本迭代成功")
      loadVersionList()
      getList() // 刷新主列表
    }).catch(err => {
      proxy.$modal.msgError(err.msg || "版本迭代失败")
    })
  }).catch(() => {})
}

// 查看实例（按flowCode查看所有实例）
function openViewInstanceByCode(row) {
  instanceQueryParams.flowId = null
  instanceQueryParams.flowCode = row.flowCode
  instanceQueryParams.pageNum = 1
  instanceDialogOpen.value = true
  getInstanceList()
}

// 查看实例（按flowId查看特定版本的实例）
function openViewInstanceByFlowId(row) {
  instanceQueryParams.flowId = row.id
  instanceQueryParams.flowCode = null
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

function handlePublish(row) {
  proxy.$modal.confirm("确认发布该流程吗？发布后将无法修改，只能进行版本迭代。").then(() => {
    publishFlowDef(row.id).then(() => {
      proxy.$modal.msgSuccess("发布成功")
      getList()
    }).catch(err => {
      proxy.$modal.msgError(err.msg || "发布失败")
    })
  }).catch(() => {})
}

function handleIterateFromList() {
  const id = ids.value[0]
  if (!id) {
    proxy.$modal.msgError("请选择一条记录")
    return
  }
  const row = dataList.value.find(item => item.id === id)
  if (row) {
    openIterateDialog(row)
  }
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
