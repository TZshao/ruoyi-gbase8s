<template>
  <el-row :gutter="20">
    <el-col :span="8">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>表列表</span>
            <el-button type="primary" size="small" @click="loadTables" :loading="tableLoading">
              刷新
            </el-button>
          </div>
        </template>
        <el-table
          :data="tableList"
          highlight-current-row
          @current-change="handleTableChange"
          style="width: 100%"
          height="500"
        >
          <el-table-column prop="name" label="表名" />
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="16">
      <el-card>
        <template #header>
          <span>表结构 - {{ currentTableName || '请选择表' }}</span>
        </template>
        <el-table
          :data="columnList"
          style="width: 100%"
          height="500"
          v-loading="columnLoading"
        >
          <el-table-column prop="columnName" label="列名" width="180" />
          <el-table-column prop="columnType" label="数据类型" width="150" />
          <el-table-column prop="isRequired" label="是否必填" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.isRequired === '1' ? 'danger' : 'success'">
                {{ scope.row.isRequired === '1' ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="columnComment" label="默认值" />
        </el-table>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup name="SqlServerDataSourceExample">
import { ref, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { listTables, getTableColumns } from "@/api/tool/dataSource"

const tableList = ref([])
const columnList = ref([])
const currentTableName = ref("")
const tableLoading = ref(false)
const columnLoading = ref(false)

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

// 处理表选择变化
const handleTableChange = async (row) => {
  if (!row) {
    currentTableName.value = ""
    columnList.value = []
    return
  }
  
  currentTableName.value = row.name
  columnLoading.value = true
  try {
    const response = await getTableColumns(row.name)
    if (response.code === 200) {
      columnList.value = response.data || []
      if (columnList.value.length > 0) {
        ElMessage.success(`加载表结构成功，共 ${columnList.value.length} 个字段`)
      } else {
        ElMessage.warning("该表没有字段信息")
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

// 组件挂载时自动加载表列表
onMounted(() => {
  loadTables()
})
</script>

<style scoped>
</style>
