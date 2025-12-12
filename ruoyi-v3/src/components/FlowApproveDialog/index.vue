<template>
  <el-dialog
    :title="dialogTitle"
    width="960px"
    :model-value="modelValue"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="steps-card" v-loading="loading">
          <div class="steps-title">
            当前步骤：<span class="step-code">{{ currentStepCode || "-" }}</span>
          </div>
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
        <el-card shadow="never" class="mb12" v-loading="loading || actionLoading">
          <FlowFrom
            ref="formFromRef"
            :form-schema="formSchema"
            v-model="formData"
            :current-step="currentStepCode"
            label-width="110px"
          />
        </el-card>
        <el-card shadow="never" v-loading="actionLoading">
          <div class="steps-title">审批意见</div>
          <el-input
            v-model="comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </el-card>
      </el-col>
    </el-row>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button :loading="actionLoading" type="danger" @click="handleReject">拒绝</el-button>
      <el-button :loading="actionLoading" type="primary" @click="handlePass">通过</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="FlowApproveDialog">
import { computed, ref, watch } from "vue"
import { getFlowDef } from "@/api/flow/definition"
import { getInstance, approveInstance } from "@/api/flow/instance"
import { listFlowStep } from "@/api/flow/step"
import FlowFrom from "@/components/FlowFrom"

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  instanceId: {
    type: [Number, String],
    required: true
  },
  title: {
    type: String,
    default: "流程审批"
  }
})

const emit = defineEmits(["update:modelValue", "approved"])

const formFromRef = ref(null)
const formSchema = ref({ fields: [] })
const formData = ref({})
const stepList = ref([])
const currentStepCode = ref("")
const flowId = ref()
const instanceId = ref()
const comment = ref("")
const loading = ref(false)
const actionLoading = ref(false)

const dialogTitle = computed(() => props.title || "流程审批")
const activeStepIndex = computed(() => {
  const idx = stepList.value.findIndex(step => step.stepCode === currentStepCode.value)
  return idx >= 0 ? idx : 0
})

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      console.log(props.instanceId, 'instanceId')
      initData()
    }
  },
  { immediate: true }
)

function handleClose() {
  emit("update:modelValue", false)
}

async function initData() {
  loading.value = true
  try {
    console.log(props.instanceId, 'instanceId')
    const res = await getInstance(props.instanceId)
    const instance = res.data || {}
    instanceId.value = instance.id || props.instanceId
    flowId.value = instance.flowId
    currentStepCode.value = instance.currentStepCode
    formData.value = safeParse(instance.formData)
    comment.value = ""
    await Promise.all([
      loadFlowDef(flowId.value),
      loadSteps(flowId.value)
    ])
  } finally {
    loading.value = false
  }
}

async function loadFlowDef(id) {
  if (!id) {
    formSchema.value = { fields: [] }
    return
  }
  const res = await getFlowDef(id)
  formSchema.value = safeParse(res.data?.formSchema, { fields: [] })
}

async function loadSteps(id) {
  if (!id) {
    stepList.value = []
    return
  }
  const res = await listFlowStep({ flowId: id, pageNum: 1, pageSize: 200 })
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

async function handlePass() {
  await submitAction(true)
}

async function handleReject() {
  await submitAction(false)
}

async function submitAction(pass) {
  await validateForm()
  const targetId = instanceId.value || props.instanceId
  if (!targetId) {
    console.warn("instanceId missing, skip submit")
    return
  }
  actionLoading.value = true
  try {
    await approveInstance(targetId, { instanceId: targetId, pass, comment: comment.value })
    emit("approved", { pass })
    handleClose()
  } finally {
    actionLoading.value = false
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
.step-code {
  color: var(--el-color-primary);
}
.mb12 {
  margin-bottom: 12px;
}
</style>

