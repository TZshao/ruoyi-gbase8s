<template>
  <el-dialog
    :title="dialogTitle"
    width="900px"
    :model-value="modelValue"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="steps-card" v-loading="loading">
          <div class="steps-title">流程步骤</div>
          <el-steps direction="vertical" :active="activeStepIndex" finish-status="success">
            <el-step
              v-for="step in stepList"
              :key="step.stepCode"
              :title="`${step.orderNum ?? ''} ${step.stepName}`.trim()"
              :description="`编码：${step.stepCode}`"
            />
          </el-steps>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" v-loading="loading || saving">
          <FlowFrom
            ref="formFromRef"
            :form-schema="formSchema"
            v-model="formData"
            :current-step="currentStepCode"
            label-width="110px"
          />
        </el-card>
      </el-col>
    </el-row>
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
import FlowFrom from "@/components/FlowFrom"
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
      formData.value = safeParse(instance.formData)
    } else {
      resolvedInstanceId.value = null
      instanceStatus.value = ""
      currentStepCode.value = "RISE"
      formData.value = {}
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
        emit("saved")
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
  height: 100%;
}
.steps-title {
  font-weight: 600;
  margin-bottom: 12px;
}
</style>

