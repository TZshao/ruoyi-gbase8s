<template>
  <div class="custom-form">
    <el-form ref="formRef" :model="localData" :rules="rules" label-width="120px">
      <!-- 示例：标题 -->
      <el-form-item label="标题" prop="title">
        <el-input v-model="localData.title" placeholder="请输入标题" />
      </el-form-item>

      <!-- 示例：金额 只在第一步可以填写 -->
      <el-form-item label="金额" :prop="currentStep==='RISE' ? 'amount' : undefined">
        <el-input-number
          v-if="currentStep==='RISE'"
          v-model="localData.amount"
          :min="0"
          :precision="2"
          :step="100"
          style="width: 100%"
        />
        <span v-else class="readonly-text">{{ localData.amount !== null && localData.amount !== undefined ? localData.amount : '-' }}</span>
      </el-form-item>



      <!-- 示例：说明 -->
      <el-form-item label="说明">
        <el-input
          v-model="localData.remark"
          type="textarea"
          :rows="3"
          placeholder="可选说明"
        />
      </el-form-item>

    </el-form>
  </div>
</template>

<script setup>
import { ref, watch } from "vue"

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) }, // v-model：表单数据双向绑定，用于接收和更新表单数据
  currentStep: { type: String, default: "" } // 当前流程步骤编码，可用于控制字段的显示/编辑状态
})
const emit = defineEmits(["update:modelValue"])

const formRef = ref(null)
const localData = ref({
  title: "",
  amount: null,
  remark: "",
  ...props.modelValue
})

const rules = {
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  amount: [
    { required: true, message: "请输入金额", trigger: "change" },
    {
      validator: (_, val, cb) => {
        if (val === null || val === undefined) return cb(new Error("金额不能为空"))
        if (Number(val) < 0) return cb(new Error("金额需大于等于0"))
        cb()
      },
      trigger: "change"
    }
  ]
}

// 本地 -> v-model
watch(localData, (val) => emit("update:modelValue", { ...val }), { deep: true })

// 外部 -> 本地
watch(
  () => props.modelValue,
  (val) => {
    if (val && JSON.stringify(val) !== JSON.stringify(localData.value)) {
      localData.value = { ...localData.value, ...val }
    }
  },
  { deep: true }
)

// 暴露 validate 给外层（FlowFrom 会在提交/审批时调用）
async function validate() {
  if (formRef.value) {
    await formRef.value.validate()
  }
  return localData.value
}

defineExpose({ validate })
</script>

<style scoped>
.custom-form {
  padding: 8px 4px;
}
.readonly-text {
  color: #606266;
  line-height: 32px;
}
</style>
