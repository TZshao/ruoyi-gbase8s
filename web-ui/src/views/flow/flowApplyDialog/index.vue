<template>
  <el-dialog
    :title="dialogTitle"
    width="1200px"
    :model-value="modelValue"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
      <el-steps :active="activeStepIndex" finish-status="success" simple align-center style="margin-bottom: 10px;">
        <el-step
          v-for="step in stepList"
          :key="step.stepCode"
          :title="`${step.orderNum ?? ''} ${step.stepName}`.trim()"
        />
      </el-steps>

    <el-card shadow="never" v-loading="loading || saving" class="form-card">
      <FlowFrom
        ref="formFromRef"
        :form-schema="formSchema"
        v-model="formData"
        :current-step="currentStepCode"
        label-width="110px"
      />
    </el-card>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button :loading="saving" @click="handleSave">暂存</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="FlowApplyDialog">
import { computed, ref, watch } from "vue"
import { getFlowDef } from "@/api/flow/definition"
import { getInstance, startInstance, updateInstance, submitInstance } from "@/api/flow/instance"
import { listFlowStep } from "@/api/flow/step"
import FlowFrom from "@/views/flow/flowFrom/from.vue"
import { ElMessage } from "element-plus"

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  flowId: [Number, String],
  instanceId: [Number, String],
  title: {
    type: String,
    default: "发起申请"
  },
  // 初始表单数据，用于外部传入数据（如项目ID等）
  // 这些数据会合并到表单数据中并保存，无需在表单配置中设置 visible=false 的隐藏字段
  // 如果需要在表单中显示但不允许编辑，可以在表单配置中添加字段并设置 enableIn=[]（不在任何步骤可编辑）
  initialFormData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(["update:modelValue", "saved", "submitted"])

const formFromRef = ref(null)
const formSchema = ref({ fields: [] })
const formData = ref({})
const stepList = ref([])
const currentStepCode = ref("RISE")
const resolvedFlowId = ref()
const resolvedInstanceId = ref()
const instanceStatus = ref("")
const loading = ref(false)
const saving = ref(false)

const dialogTitle = computed(() => props.title || "发起申请")
const activeStepIndex = computed(() => {
  const idx = stepList.value.findIndex(step => step.stepCode === currentStepCode.value)
  return idx >= 0 ? idx : 0
})

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      initData()
    }
  }
)

function handleClose() {
  emit("update:modelValue", false)
}

async function initData() {
  loading.value = true
  try {
    let flowId = props.flowId
    if (props.instanceId) {
      const res = await getInstance(props.instanceId)
      const instance = res.data || {}
      flowId = instance.flowId
      resolvedInstanceId.value = instance.id
      instanceStatus.value = instance.status || ""
      currentStepCode.value = instance.currentStepCode || "RISE"
      // 合并实例数据和外部传入的初始数据（外部数据优先级更高，会覆盖实例中的同名字段）
      const instanceFormData = safeParse(instance.formData)
      formData.value = { ...instanceFormData, ...props.initialFormData }
    } else {
      resolvedInstanceId.value = null
      instanceStatus.value = ""
      currentStepCode.value = "RISE"
      // 新建流程时，使用外部传入的初始数据
      // 这些数据会传递给 FlowFrom 组件，即使不在 schema 中定义也会被保存
      formData.value = { ...props.initialFormData }
    }
    resolvedFlowId.value = flowId
    await Promise.all([
      loadFlowDef(flowId),
      loadSteps(flowId)
    ])
  } finally {
    loading.value = false
  }
}

async function loadFlowDef(flowId) {
  if (!flowId) {
    formSchema.value = { fields: [] }
    return
  }
  const res = await getFlowDef(flowId)
  const schema = res.data?.formSchema
  formSchema.value = safeParse(schema, { fields: [] })
}

async function loadSteps(flowId) {
  if (!flowId) {
    stepList.value = []
    return
  }
  const res = await listFlowStep({ flowId, pageNum: 1, pageSize: 200 })
  stepList.value = res.rows || []
}

function safeParse(val, fallback = {}) {
  if (!val) return fallback
  if (typeof val === "object") return val
  try {
    return JSON.parse(val)
  } catch (e) {
    return fallback
  }
}

async function validateForm() {
  if (formFromRef.value?.validate) {
    await formFromRef.value.validate()
  }
  return true
}

async function handleSave() {
  await saveOrUpdateInstance(false)
}

async function handleSubmit() {
  await saveOrUpdateInstance(true)
}

async function saveOrUpdateInstance(isSubmit) {
  await validateForm()
  if (!resolvedFlowId.value) return
  saving.value = true
  try {
    const payload = {
      flowId: resolvedFlowId.value,
      formData: JSON.stringify(formData.value || {})
    }

    if (resolvedInstanceId.value) {
      // 更新已存在的实例
      payload.id = resolvedInstanceId.value
      await updateInstance(payload)

      // 如果是提交操作，调用提交接口
      if (isSubmit) {
        await submitInstance(resolvedInstanceId.value)
        ElMessage.success("提交成功")
        emit("submitted")
        handleClose()
      } else {
        ElMessage.success("暂存成功")
        emit("saved") //出发父组件 @saved方法，父组件就可以执行 getList，or others
      }
    } else {
      // 创建新实例
      const res = await startInstance(payload)
      const instance = res.data
      const newInstanceId = instance?.id

      // 如果是提交操作，调用提交接口
      if (isSubmit && newInstanceId) {
        await submitInstance(newInstanceId)
        ElMessage.success("提交成功")
        emit("submitted")
        handleClose()
      } else {
        ElMessage.success("暂存成功")
        emit("saved")
        // 更新实例ID，以便下次可以继续编辑
        if (newInstanceId) {
          resolvedInstanceId.value = newInstanceId
        }
      }
    }
  } catch (error) {
    ElMessage.error(error.message || "操作失败")
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.steps-card {
  margin-bottom: 12px;
}
.steps-title {
  font-weight: 600;
  margin-bottom: 12px;
}
.form-card {
  margin-top: 0;
}
</style>

