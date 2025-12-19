<template>
  <div class="app-container">
    <!-- 顶部：源表列表 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>源表列表</span>
          <div style="display: flex; align-items: center; gap: 10px;">
            <el-input
              v-model="tableSearchKeyword"
              placeholder="搜索表名"
              clearable
              style="width: 200px;"
              @input="handleTableSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" size="small" @click="loadTables" :loading="tableLoading">
              刷新
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="filteredTableList"
        highlight-current-row
        @current-change="handleTableChange"
        style="width: 100%"
        border
        v-loading="tableLoading"
        :max-height="400"
      >
        <el-table-column prop="name" label="表名" />
      </el-table>
    </el-card>

    <el-row :gutter="20">

      <!-- 迁移配置 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>迁移配置</span>
          </template>

          <!-- 表结构 -->
          <div v-if="currentTableName" style="margin-bottom: 20px;">
            <h4>表结构 - {{ currentTableName }}</h4>
            <el-table
                :data="columnList"
                style="width: 100%"
                border
                v-loading="columnLoading"
            >
              <el-table-column prop="columnName" label="列名" width="150" />
              <el-table-column prop="columnType" label="数据类型" width="120" />
              <el-table-column prop="columnComment" label="注释" />
              <el-table-column prop="isRequired" label="是否必填" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.isRequired === '1' ? 'danger' : 'success'">
                    {{ scope.row.isRequired === '1' ? '是' : '否' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 实体类选择 -->
        </el-card>
      </el-col>

      <!-- 字段映射 -->
      <el-col :span="12">
        <el-card >
          <template #header>
            <span>字段映射</span>
          </template>
          <el-form :model="migrationForm" label-width="120px" style="margin-top: 20px;">
            <el-form-item label="实体类">
              <el-select
                  v-model="migrationForm.entityClassName"
                  placeholder="请选择实体类"
                  style="width: 100%"
                  @change="handleEntityClassChange"
                  :loading="entityClassLoading"
              >
                <el-option
                    v-for="item in entityClassList"
                    :key="item.fullClassName"
                    :label="item.className"
                    :value="item.fullClassName"
                />
              </el-select>
            </el-form-item>

            <!-- Mapper方法选择 -->
            <el-form-item label="Mapper方法">
              <el-select
                  v-model="migrationForm.methodName"
                  placeholder="请选择Mapper方法"
                  style="width: 100%"
                  @change="handleMethodChange"
                  :loading="methodLoading"
                  :disabled="!migrationForm.entityClassName"
              >
                <el-option
                    v-for="item in filteredMapperMethods"
                    :key="item.methodName"
                    :label="`${item.mapperClassName.split('.').pop()}.${item.methodName}`"
                    :value="item.methodName"
                >
                  <span>{{ item.mapperClassName.split('.').pop() }}.</span>
                  <span style="font-weight: bold;">{{ item.methodName }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>

          <el-table v-if="columnList.length > 0 && entityFieldList.length > 0"
                    :data="fieldMappingList"
                    style="width: 100%"
                    border
          >
            <el-table-column prop="sourceField" label="源表字段" width="200">
              <template #default="scope">
                <span>{{ getSourceFieldDisplay(scope.row) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sourceField" width="50">
              <template #default="socpe">
                <span> <el-icon><DArrowRight /></el-icon> </span>
              </template>
            </el-table-column>

            <el-table-column prop="targetField" label="实体类字段" width="200">
              <template #default="scope">
                <el-select
                    v-model="scope.row.targetField"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                >
                  <el-option
                      v-for="field in entityFieldList"
                      :key="field.fieldName"
                      :label="`${field.fieldName} (${field.fieldType})`"
                      :value="field.fieldName"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="fieldType" label="字段类型" width="100">
              <template #default="scope">
                <el-tag size="small">{{ getFieldType(scope.row.targetField) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

    </el-row>

    <!-- 底部：操作按钮 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>操作</span>
      </template>
      <div style="text-align: center;">
        <el-button
          type="primary"
          size="large"
          @click="handleValidate"
          :loading="validating"
          :disabled="!canValidate"
          style="margin-right: 20px;"
        >
          验证
        </el-button>
        <el-button
          type="success"
          size="large"
          @click="handleMigrate"
          :loading="migrating"
          :disabled="!canMigrate"
        >
          转换
        </el-button>
      </div>
    </el-card>

    <!-- 右侧：验证结果和迁移进度 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>验证结果</span>
          </template>
          <div v-if="validationResult">
            <el-alert
              :type="validationResult.passed ? 'success' : 'error'"
              :title="validationResult.message"
              :closable="false"
              style="margin-bottom: 15px;"
            />
            <div style="margin-bottom: 10px;">
              <p>总数: {{ validationResult.totalCount }}</p>
              <p>成功: <span style="color: green;">{{ validationResult.successCount }}</span></p>
              <p>失败: <span style="color: red;">{{ validationResult.failureCount }}</span></p>
            </div>
            <el-collapse v-if="validationResult.errors && validationResult.errors.length > 0">
              <el-collapse-item title="错误详情" name="errors">
                <div
                  v-for="error in validationResult.errors"
                  :key="error.rowNumber"
                  style="margin-bottom: 10px; padding: 10px; background: #f5f5f5; border-radius: 4px;"
                >
                  <p><strong>第 {{ error.rowNumber }} 行:</strong></p>
                  <p style="color: red;">{{ error.errorMessage }}</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
          <div v-else style="color: #999; text-align: center; padding: 20px;">
            请先进行验证
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>迁移进度</span>
          </template>
          <div v-if="migrationResult">
            <el-alert
              type="info"
              :title="migrationResult.message"
              :closable="false"
              style="margin-bottom: 15px;"
            />
            <div style="margin-bottom: 10px;">
              <p>总数: {{ migrationResult.totalCount }}</p>
              <p>成功: <span style="color: green;">{{ migrationResult.successCount }}</span></p>
              <p>失败: <span style="color: red;">{{ migrationResult.failureCount }}</span></p>
            </div>
            <el-collapse v-if="migrationResult.errorDetails && migrationResult.errorDetails.length > 0">
              <el-collapse-item title="错误详情" name="errors">
                <div
                  v-for="(error, index) in migrationResult.errorDetails"
                  :key="index"
                  style="margin-bottom: 10px; padding: 10px; background: #f5f5f5; border-radius: 4px;"
                >
                  <p style="color: red;">{{ error }}</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
          <div v-else style="color: #999; text-align: center; padding: 20px;">
            请先进行转换
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="DataMigration">
import { ref, computed, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { Search } from "@element-plus/icons-vue"
import { listTables, getTableColumns } from "@/api/tool/dataSource"
import {
  getEntityClasses,
  getEntityFields,
  getMapperMethods,
  validateMigration,
  migrateData
} from "@/api/tool/migration"

// 表相关
const tableList = ref([])
const columnList = ref([])
const currentTableName = ref("")
const tableLoading = ref(false)
const columnLoading = ref(false)
const tableSearchKeyword = ref("")

// 实体类相关
const entityClassList = ref([])
const entityClassLoading = ref(false)
const entityFieldList = ref([])

// Mapper方法相关
const mapperMethodList = ref([])
const methodLoading = ref(false)

// 迁移表单
const migrationForm = ref({
  entityClassName: "",
  mapperClassName: "",
  methodName: ""
})

// 字段映射列表
const fieldMappingList = ref([])

// 验证相关
const validationResult = ref(null)
const validating = ref(false)

// 迁移相关
const migrationResult = ref(null)
const migrating = ref(false)

// 计算属性
const filteredMapperMethods = computed(() => {
  if (!migrationForm.value.entityClassName) {
    return []
  }
  const entitySimpleName = migrationForm.value.entityClassName.split('.').pop()
  return mapperMethodList.value.filter(method =>
    method.parameterTypeSimpleName === entitySimpleName
  )
})

const canValidate = computed(() => {
  return currentTableName.value &&
         migrationForm.value.entityClassName &&
         migrationForm.value.methodName &&
         fieldMappingList.value.some(m => m.targetField)
})

const canMigrate = computed(() => {
  return canValidate.value && validationResult.value && validationResult.value.passed
})

// 过滤后的表列表
const filteredTableList = computed(() => {
  if (!tableSearchKeyword.value) {
    return tableList.value
  }
  const keyword = tableSearchKeyword.value.toLowerCase()
  return tableList.value.filter(table =>
    table.name.toLowerCase().includes(keyword)
  )
})

// 加载表列表
const loadTables = async () => {
  tableLoading.value = true
  try {
    const response = await listTables()
    if (response.code === 200) {
      tableList.value = response.data.map(name => ({ name }))
      if (tableList.value.length > 0) {
        ElMessage.success(`加载表列表成功，共 ${tableList.value.length} 个表`)
      } else {
        ElMessage.warning("未找到任何表")
      }
    } else {
      ElMessage.error(response.msg || "加载表列表失败")
    }
  } catch (error) {
    ElMessage.error("加载表列表失败：" + error.message)
  } finally {
    tableLoading.value = false
  }
}

// 处理表搜索
const handleTableSearch = () => {
  // 搜索时自动触发，通过computed属性过滤
}

// 处理表选择变化
const handleTableChange = async (row) => {
  if (!row) {
    currentTableName.value = ""
    columnList.value = []
    fieldMappingList.value = []
    return
  }

  currentTableName.value = row.name
  columnLoading.value = true
  try {
    const response = await getTableColumns(row.name)
    if (response.code === 200) {
      columnList.value = response.data || []
      // 初始化字段映射列表
      fieldMappingList.value = columnList.value.map(col => ({
        sourceField: col.columnName,
        targetField: ""
      }))
      if (columnList.value.length > 0) {
        ElMessage.success(`加载表结构成功，共 ${columnList.value.length} 个字段`)
      }
    } else {
      ElMessage.error(response.msg || "加载表结构失败")
      columnList.value = []
    }
  } catch (error) {
    ElMessage.error("加载表结构失败：" + error.message)
    columnList.value = []
  } finally {
    columnLoading.value = false
  }
}

// 加载实体类列表
const loadEntityClasses = async () => {
  entityClassLoading.value = true
  try {
    const response = await getEntityClasses()
    if (response.code === 200) {
      entityClassList.value = response.data || []
    } else {
      ElMessage.error(response.msg || "加载实体类列表失败")
    }
  } catch (error) {
    ElMessage.error("加载实体类列表失败：" + error.message)
  } finally {
    entityClassLoading.value = false
  }
}

// 处理实体类变化
const handleEntityClassChange = async (className) => {
  if (!className) {
    entityFieldList.value = []
    return
  }

  try {
    const response = await getEntityFields(className)
    if (response.code === 200) {
      entityFieldList.value = response.data || []
    } else {
      ElMessage.error(response.msg || "加载实体类字段失败")
    }
  } catch (error) {
    ElMessage.error("加载实体类字段失败：" + error.message)
  }
}

// 加载Mapper方法列表
const loadMapperMethods = async () => {
  methodLoading.value = true
  try {
    const response = await getMapperMethods()
    if (response.code === 200) {
      mapperMethodList.value = response.data || []
    } else {
      ElMessage.error(response.msg || "加载Mapper方法列表失败")
    }
  } catch (error) {
    ElMessage.error("加载Mapper方法列表失败：" + error.message)
  } finally {
    methodLoading.value = false
  }
}

// 处理方法变化
const handleMethodChange = (methodName) => {
  const method = filteredMapperMethods.value.find(m => m.methodName === methodName)
  if (method) {
    migrationForm.value.mapperClassName = method.mapperClassName
  }
}

// 获取字段类型
const getFieldType = (fieldName) => {
  if (!fieldName) return ""
  const field = entityFieldList.value.find(f => f.fieldName === fieldName)
  return field ? field.fieldType : ""
}

// 获取源表字段显示（字段名(类型)）
const getSourceFieldDisplay = (row) => {
  const column = columnList.value.find(col => col.columnName === row.sourceField)
  if (column && column.columnType) {
    return `${row.sourceField}  [${column.columnType}]`
  }
  return row.sourceField
}

// 验证
const handleValidate = async () => {
  if (!canValidate.value) {
    ElMessage.warning("请先完成配置")
    return
  }

  validating.value = true
  try {
    const request = {
      tableName: currentTableName.value,
      entityClassName: migrationForm.value.entityClassName,
      mapperClassName: migrationForm.value.mapperClassName,
      methodName: migrationForm.value.methodName,
      fieldMappings: fieldMappingList.value.filter(m => m.targetField),
      validateLimit: 10
    }

    const response = await validateMigration(request)
    if (response.code === 200) {
      validationResult.value = response.data
      if (validationResult.value.passed) {
        ElMessage.success("验证通过")
      } else {
        ElMessage.warning("验证失败，请查看错误详情")
      }
    } else {
      ElMessage.error(response.msg || "验证失败")
    }
  } catch (error) {
    ElMessage.error("验证失败：" + error.message)
  } finally {
    validating.value = false
  }
}

// 迁移
const handleMigrate = async () => {
  if (!canMigrate.value) {
    ElMessage.warning("请先完成验证且验证通过")
    return
  }

  if (!confirm("确定要执行数据迁移吗？")) {
    return
  }

  migrating.value = true
  try {
    const request = {
      tableName: currentTableName.value,
      entityClassName: migrationForm.value.entityClassName,
      mapperClassName: migrationForm.value.mapperClassName,
      methodName: migrationForm.value.methodName,
      fieldMappings: fieldMappingList.value.filter(m => m.targetField)
    }

    const response = await migrateData(request)
    if (response.code === 200) {
      migrationResult.value = response.data
      ElMessage.success("迁移完成")
    } else {
      ElMessage.error(response.msg || "迁移失败")
    }
  } catch (error) {
    ElMessage.error("迁移失败：" + error.message)
  } finally {
    migrating.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadTables()
  loadEntityClasses()
  loadMapperMethods()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
