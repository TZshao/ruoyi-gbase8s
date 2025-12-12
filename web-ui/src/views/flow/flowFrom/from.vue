<template>
<!-- 假如使用自定页面的表单，相当于简单的嵌套，不会动态渲染字段 -->
  <component
    v-if="isCustomPageEnabled && customComponent && !customComponentError"
    :is="customComponent"
    v-model="formData"
    :current-step="currentStep"
    ref="customPageRef"
  />
  <el-alert
    v-if="isCustomPageEnabled && customComponentError"
    type="error"
    :closable="false"
    :title="`自定义组件加载失败: ${customComponentError}`"
  />
  <el-alert
    v-else-if="customPageConfig?.enabled && !customComponent && !customComponentError"
    type="warning"
    :closable="false"
    title="已启用自定义页面，但未配置组件路径"
  />

<!--  根据json表单配置动态渲染字段-->
  <el-form
    v-else
    ref="formRef"
    :model="formData"
    :rules="formRules"
    :label-width="labelWidth"
  >
    <el-row>
      <el-col :span="field.type === 'text' || field.type === 'file' ? 24 : colSpan" v-for="field in schemaFields" :key="field.name">
        <el-form-item :label="field.label" :prop="isEditable(field) ? field.name : undefined">
          <!-- 不可编辑时显示为只读文本或文件链接 -->
          <template v-if="!isEditable(field)">
            <!-- 文件类型：显示为可点击链接列表，每个文件单独一行 -->
            <div v-if="field.type === 'file'" class="readonly-file-list">
              <div v-if="getFileList(field).length === 0" class="readonly-text">-</div>
              <el-link
                v-for="(file, index) in getFileList(field)"
                :key="index"
                :href="getFileUrl(file)"
                target="_blank"
                class="readonly-file-link"
              >
                <el-icon><Document /></el-icon>
                {{ getFileName(file) }}
              </el-link>
            </div>
            <!-- 长文本类型：单独一行，支持换行 -->
            <div v-else-if="field.type === 'text'" class="readonly-text-block">
              {{ formatFieldValue(field) }}
            </div>
            <!-- 其他类型：显示为只读文本 -->
            <span v-else class="readonly-text">{{ formatFieldValue(field) }}</span>
          </template>

          <!-- 可编辑时显示输入控件 -->
          <template v-else>
            <!-- 下拉框 -->
            <el-select
              v-if="field.type === 'select'"
              v-model="formData[field.name]"
              :placeholder="'请选择' + field.label"
              style="width: 100%"
            >
              <el-option
                v-for="option in fieldOptionsMap[field.name] || []"
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
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />

            <!-- 数字输入框 -->
            <el-input-number
              v-else-if="field.type === 'number' && !field.readonly"
              v-model="formData[field.name]"
              :placeholder="'请输入' + field.label"
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
            />

            <!-- 文件上传 -->
            <file-upload
              v-else-if="field.type === 'file'"
              v-model="formData[field.name]"
            />

            <!-- 字符串输入框（默认） -->
            <el-input
              v-else
              v-model="formData[field.name]"
              :placeholder="'请输入' + field.label"
            />
          </template>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup name="FlowFrom">
import { computed, watch, ref, defineAsyncComponent, shallowRef } from "vue"
import { Document } from "@element-plus/icons-vue"
import FileUpload from "@/components/FileUpload"
import { getDicts } from "@/api/system/dict/data"

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
const customPageRef = ref(null)
const formData = ref({})
const formRules = ref({})
const customComponent = shallowRef(null)
const customComponentError = ref(null)
// 字典数据缓存，key为字典类型，value为选项数组
const dictDataCache = ref({})
// 字段选项映射，key为字段name，value为选项数组
const fieldOptionsMap = ref({})

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

const customPageConfig = computed(() => schemaObj.value?.customPage || null)
const isCustomPageEnabled = computed(() => !!(customPageConfig.value && customPageConfig.value.enabled))

watch(customPageConfig, async (cfg) => {
  if (cfg?.enabled && cfg.component) {
    customComponentError.value = null
    let componentPath = cfg.component

    // 修正路径：@/view -> @/views
    if (componentPath.startsWith('@/view/') && !componentPath.startsWith('@/views/')) {
      componentPath = componentPath.replace('@/view/', '@/views/')
    }

    // 将别名路径转换为绝对路径（/src/...），Vite 在开发和生产环境都支持
    if (componentPath.startsWith('@/')) {
      componentPath = componentPath.replace('@/', '/src/')
    }

    // 加载组件
    try {
      await import(/* @vite-ignore */ componentPath)
      // 加载成功，创建异步组件
      customComponent.value = defineAsyncComponent(() => import(/* @vite-ignore */ componentPath))
    } catch (error) {
      console.error('自定义组件加载失败:', error)
      customComponentError.value = error.message || `组件路径不存在: ${componentPath}`
      customComponent.value = null
    }
  } else {
    customComponent.value = null
    customComponentError.value = null
  }
}, { immediate: true, deep: true })

// 获取所有字段列表（确保为数组）
const schemaFields = computed(() => {
  return Array.isArray(schemaObj.value?.fields) ? schemaObj.value.fields : []
})

// 判断字段是否可编辑
function isEditable(field) {
  if (!props.currentStep || !field.enableIn) {
    return false // 如果没有当前步骤或enableIn，不可编辑
  }
  // enableIn 可以是数组或逗号分隔的步骤编码列表
  const enabledSteps = Array.isArray(field.enableIn)
    ? field.enableIn.map(s => String(s).trim())
    : String(field.enableIn || "").split(',').map(s => s.trim())
  return enabledSteps.includes(props.currentStep)
}

// 加载字典选项
async function loadDictOptions(dictType) {
  // 如果缓存中有，直接返回
  if (dictDataCache.value[dictType]) {
    return dictDataCache.value[dictType]
  }
  // 从API加载字典数据
  try {
    const res = await getDicts(dictType)
    const options = (res.data || []).map(item => ({
      label: item.dictLabel,
      value: item.dictValue
    }))
    // 缓存字典数据
    dictDataCache.value[dictType] = options
    return options
  } catch (error) {
    console.error('加载字典数据失败:', error)
    return []
  }
}

// 加载字段选项
async function loadFieldOptions(field) {
  if (!field || !field.name) return
  
  // 如果字段有options属性，直接使用
  if (field.options && Array.isArray(field.options)) {
    fieldOptionsMap.value[field.name] = field.options
    return
  }
  
  // 如果字段有字典类型配置，加载字典数据
  if (field.type === 'select' && field.dictType) {
    const options = await loadDictOptions(field.dictType)
    fieldOptionsMap.value[field.name] = options
    return
  }
  
  // 如果没有options和字典类型，设置为空数组
  fieldOptionsMap.value[field.name] = []
}

// 加载所有字段的选项
async function loadAllFieldOptions() {
  const fields = Array.isArray(schemaFields.value) ? schemaFields.value : []
  const selectFields = fields.filter(f => f.type === 'select')
  
  // 并行加载所有下拉字段的选项
  await Promise.all(selectFields.map(field => loadFieldOptions(field)))
}

// 获取文件列表（解析文件字段值）
function getFileList(field) {
  const value = formData.value[field.name]
  if (!value) {
    return []
  }
  // 如果是数组，直接返回
  if (Array.isArray(value)) {
    return value.map(item => {
      if (typeof item === 'string') {
        return { name: item, url: item }
      }
      return item
    })
  }
  // 如果是字符串（逗号分隔），转换为数组
  if (typeof value === 'string') {
    return value.split(',').filter(url => url.trim()).map(url => {
      const trimmed = url.trim()
      return { name: trimmed, url: trimmed }
    })
  }
  return []
}

// 获取文件URL（添加baseUrl前缀）
function getFileUrl(file) {
  if (!file || !file.url) {
    return '#'
  }
  const baseUrl = import.meta.env.VITE_APP_BASE_API || ''
  // 如果URL已经包含baseUrl，直接返回；否则拼接
  if (file.url.startsWith('http://') || file.url.startsWith('https://') || file.url.startsWith(baseUrl)) {
    return file.url
  }
  return baseUrl + file.url
}

// 获取文件名
function getFileName(file) {
  if (!file) return ''
  const name = file.name || file.url || ''
  // 如果是URL，取最后一部分作为文件名
  if (name.lastIndexOf('/') > -1) {
    return name.slice(name.lastIndexOf('/') + 1)
  }
  return name
}

// 格式化字段值用于只读显示（非文件类型）
function formatFieldValue(field) {
  const value = formData.value[field.name]
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  if (field.type === 'select') {
    const options = fieldOptionsMap.value[field.name] || []
    const option = options.find(opt => opt.value === value)
    return option ? option.label : value
  }
  return value
}

// 初始化表单数据
function initFormData() {
  // 优先使用外部传入的 modelValue（包括通过 initialFormData 传入的数据）
  // 如果 modelValue 为空或没有字段，则使用当前 formData（避免覆盖已有数据）
  const externalData = props.modelValue && Object.keys(props.modelValue).length > 0 
    ? props.modelValue 
    : {}
  const currentData = formData.value && Object.keys(formData.value).length > 0
    ? formData.value
    : {}
  
  // 合并外部数据和当前数据，外部数据优先级更高
  const data = { ...currentData, ...externalData }
  
  // 确保 schema 中定义的所有字段都有默认值
  // 外部传入的数据（如项目ID、项目名称等）会被保留，不会被覆盖
  const fields = Array.isArray(schemaObj.value?.fields) ? schemaObj.value.fields : []
  fields.forEach(field => {
    // 如果字段已经有值（包括从外部传入的），保留原值
    // 只有当字段不存在于 data 中时，才设置默认值
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
  
  // 注意：data 中可能包含不在 schema 中的字段（如外部传入的项目ID等）
  // 这些字段会被保留并保存，无需在 schema 中配置隐藏字段
  // 同时，schema 中定义的字段如果 external data 中有值，也会正确回显
  formData.value = data
  emit('update:modelValue', formData.value)
}

// 构建表单验证规则（只对可编辑字段生效）
function buildRules() {
  const rules = {}
  const fields = Array.isArray(schemaFields.value) ? schemaFields.value : []
  fields.forEach(field => {
    // 只对当前步骤可编辑的字段添加校验规则
    if (isEditable(field)) {
      const required = field.required !== false
      if (required) {
        rules[field.name] = [
          { required: true, message: `请输入${field.label}`, trigger: field.type === 'select' ? 'change' : 'blur' }
        ]
      }
    }
  })
  formRules.value = rules
}

// 监听schema变化
watch(() => props.formSchema, () => {
  // 当 schema 变化时，重新初始化表单数据
  // 此时需要合并 props.modelValue 中的数据（包括 initialFormData）
  initFormData()
  buildRules()
  // 加载字段选项
  loadAllFieldOptions()
}, { immediate: true, deep: true })

// 监听当前步骤变化，重新构建校验规则
watch(() => props.currentStep, () => {
  buildRules()
})

// 监听modelValue变化（从父组件传入的数据，包括 initialFormData）
watch(() => props.modelValue, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    // 合并新值和当前值，确保 schema 中定义的字段能正确回显
    // 新值优先级更高，但会保留当前 formData 中已有的字段值
    const mergedData = { ...formData.value, ...newVal }
    
    // 如果数据有变化，更新 formData
    if (JSON.stringify(mergedData) !== JSON.stringify(formData.value)) {
      formData.value = mergedData
    }
  }
}, { deep: true, immediate: false })

// 监听formData变化，同步到父组件
watch(formData, (newVal) => {
  emit('update:modelValue', { ...newVal })
}, { deep: true })

// 验证表单
function validate() {
  return new Promise((resolve, reject) => {
    if (isCustomPageEnabled.value && customPageRef.value?.validate) {
      Promise.resolve(customPageRef.value.validate()).then(() => resolve(formData.value)).catch(reject)
      return
    }
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
.readonly-text {
  color: #606266;
  line-height: 32px;
}
.readonly-text-block {
  color: #606266;
  line-height: 24px;
  white-space: pre-wrap;
  word-break: break-word;
  width: 100%;
  display: block;
}
.readonly-file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.readonly-file-link {
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 24px;
  width: 100%;
}
</style>

