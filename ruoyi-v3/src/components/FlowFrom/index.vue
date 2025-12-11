<template>
  <el-form ref="formRef" :model="formData" :rules="formRules" :label-width="labelWidth">
    <el-row>
      <el-col :span="colSpan" v-for="field in visibleFields" :key="field.name">
        <el-form-item :label="field.label" :prop="field.name">
          <!-- 下拉框 -->
          <el-select 
            v-if="field.type === 'select'" 
            v-model="formData[field.name]" 
            :placeholder="'请选择' + field.label"
            :disabled="!isEditable(field)"
            style="width: 100%"
          >
            <el-option 
              v-for="option in getOptions(field)" 
              :key="option.value" 
              :label="option.label" 
              :value="option.value" 
            />
          </el-select>
          
          <!-- 日期选择器 -->
          <el-date-picker
            v-else-if="field.type === 'date'"
            v-model="formData[field.name]"
            type="date"
            :placeholder="'请选择' + field.label"
            :disabled="!isEditable(field)"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
          
          <!-- 数字输入框 -->
          <el-input-number
            v-else-if="field.type === 'number' && !field.readonly"
            v-model="formData[field.name]"
            :placeholder="'请输入' + field.label"
            :disabled="!isEditable(field)"
            style="width: 100%"
            controls-position="right"
          />
          
          <!-- 文本域 -->
          <el-input
            v-else-if="field.type === 'text'"
            v-model="formData[field.name]"
            type="textarea"
            :rows="field.rows || 3"
            :placeholder="'请输入' + field.label"
            :disabled="!isEditable(field)"
          />
          
          <!-- 文件上传 -->
          <file-upload
            v-else-if="field.type === 'file'"
            v-model="formData[field.name]"
            :disabled="!isEditable(field)"
          />
          
          <!-- 字符串输入框（默认） -->
          <el-input
            v-else
            v-model="formData[field.name]"
            :placeholder="'请输入' + field.label"
            :disabled="!isEditable(field)"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup name="FlowFrom">
import { computed, watch, ref } from "vue"
import FileUpload from "@/components/FileUpload"

const props = defineProps({
  // 表单schema，格式: { fields: [{name, label, type, enableIn, ...}] }
  formSchema: {
    type: [String, Object],
    default: () => ({ fields: [] })
  },
  // 表单数据
  modelValue: {
    type: Object,
    default: () => ({})
  },
  // 当前步骤编码，用于判断字段是否可编辑
  currentStep: {
    type: String,
    default: ""
  },
  // 标签宽度
  labelWidth: {
    type: String,
    default: "120px"
  },
  // 列宽度（24栅格）
  colSpan: {
    type: Number,
    default: 12
  }
})

const emit = defineEmits(["update:modelValue", "validate"])

const formRef = ref(null)
const formData = ref({})
const formRules = ref({})

// 解析schema
const schemaObj = computed(() => {
  if (!props.formSchema) {
    return { fields: [] }
  }
  if (typeof props.formSchema === 'string') {
    try {
      return JSON.parse(props.formSchema)
    } catch (e) {
      console.error('解析formSchema失败:', e)
      return { fields: [] }
    }
  }
  return props.formSchema
})

// 获取可见字段列表（确保为数组）
const visibleFields = computed(() => {
  const fields = Array.isArray(schemaObj.value?.fields) ? schemaObj.value.fields : []
  return fields.filter(field => field.visible !== 0 && field.visible !== false)
})

// 判断字段是否可编辑
function isEditable(field) {
  if (!props.currentStep || !field.enableIn) {
    return true // 如果没有当前步骤或enableIn，默认可编辑
  }
  // enableIn 可以是逗号分隔的步骤编码列表
  const enabledSteps = field.enableIn.split(',').map(s => s.trim())
  return enabledSteps.includes(props.currentStep)
}

// 获取下拉选项（如果字段有options属性）
function getOptions(field) {
  if (field.options && Array.isArray(field.options)) {
    return field.options
  }
  // 如果没有options，返回空数组
  return []
}

// 初始化表单数据
function initFormData() {
  const data = { ...props.modelValue }
  // 确保所有字段都有默认值
  const fields = Array.isArray(schemaObj.value?.fields) ? schemaObj.value.fields : []
  fields.forEach(field => {
    if (!(field.name in data)) {
      if (field.type === 'number') {
        data[field.name] = null
      } else if (field.type === 'date') {
        data[field.name] = null
      } else {
        data[field.name] = ''
      }
    }
  })
  formData.value = data
  emit('update:modelValue', formData.value)
}

// 构建表单验证规则
function buildRules() {
  const rules = {}
  const fields = Array.isArray(visibleFields.value) ? visibleFields.value : []
  fields.forEach(field => {
    const required = field.required !== false
    if (required) {
      rules[field.name] = [
        { required: true, message: `请输入${field.label}`, trigger: field.type === 'select' ? 'change' : 'blur' }
      ]
    }
  })
  formRules.value = rules
}

// 监听schema变化
watch(() => props.formSchema, () => {
  initFormData()
  buildRules()
}, { immediate: true, deep: true })

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  if (newVal && JSON.stringify(newVal) !== JSON.stringify(formData.value)) {
    formData.value = { ...newVal }
  }
}, { deep: true })

// 监听formData变化，同步到父组件
watch(formData, (newVal) => {
  emit('update:modelValue', { ...newVal })
}, { deep: true })

// 验证表单
function validate() {
  return new Promise((resolve, reject) => {
    if (formRef.value) {
      formRef.value.validate((valid) => {
        if (valid) {
          resolve(formData.value)
        } else {
          reject(new Error('表单验证失败'))
        }
      })
    } else {
      resolve(formData.value)
    }
  })
}

// 重置表单
function resetFields() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  initFormData()
}

// 暴露方法
defineExpose({
  validate,
  resetFields,
  formData
})
</script>

<style scoped>
</style>

