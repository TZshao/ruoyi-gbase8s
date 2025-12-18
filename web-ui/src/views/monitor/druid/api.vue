<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="包含系统接口">
        <el-switch
          v-model="queryParams.includeSystem"
          active-text="是"
          inactive-text="否"
          @change="handleQuery"
        />
        <span style="margin-left: 10px; color: #909399; font-size: 12px;">是否包含 /system 开头的接口</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Refresh" @click="handleQuery">刷新</el-button>
        <el-tooltip
            class="box-item"
            effect="dark"
            content="清理1000次以上的接口记录"
            placement="top"
        >
        <el-button type="warning" icon="delete" @click="handleQuery">清理</el-button>
        </el-tooltip>
      </el-form-item>
    </el-form>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center;">
                <el-icon style="margin-right: 8px;"><Timer /></el-icon>
                <span>严重耗时</span>
              </div>
              <el-switch
                  v-model="showRecent100ForSlow"
                  size="small"
                  active-text="近百次"
                  inactive-text="总计"
                  @change="handleSlowTimeTypeChange"
              />
            </div>
          </template>
          <el-table
            :data="slowApiList"
            style="width: 100%"
            max-height="300"
            @row-click="handleRowClick"
            :row-class-name="getRowClassName"
          >
            <el-table-column label="接口名称" show-overflow-tooltip min-width="200">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click.stop="handleApiClick(scope.row)">
                  {{ formatApiKey(scope.row.apiKey) }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column label="平均耗时" width="150">
              <template #header>
                <div style="display: flex; align-items: center; justify-content: space-between;">
                  <span>平均耗时</span>
                </div>
              </template>
              <template #default="scope">
                <el-tag :type="getTimeTagType(getDisplayTime(scope.row))">
                  {{ formatTime(getDisplayTime(scope.row)) }}ms
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center;">
                <el-icon style="margin-right: 8px;"><DataAnalysis /></el-icon>
                <span>高频调用</span>
              </div>
              <el-switch
                  v-model="frequentPeriod"
                  size="small"
                  active-text="近30天"
                  inactive-text="近7天"
                  :active-value=30
                  :inactive-value=7
                  @change="handleFrequentPeriodChange"
              />
            </div>
          </template>
          <el-table
            :data="frequentApiList"
            style="width: 100%"
            max-height="300"
            @row-click="handleRowClick"
            :row-class-name="getRowClassName"
          >
            <el-table-column label="接口名称" show-overflow-tooltip min-width="200">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click.stop="handleApiClick(scope.row)">
                  {{ formatApiKey(scope.row.apiKey) }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="totalCount" label="调用次数" width="100">
              <template #default="scope">
                <el-tag type="info">{{ scope.row.totalCount }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center;">
                <el-icon style="margin-right: 8px;"><Warning /></el-icon>
                <span>异常响应</span>
              </div>
              <el-button type="danger" size="small" icon="Delete" @click="handleCleanErrors">重置</el-button>
            </div>
          </template>
          <el-table
            :data="errorApiList"
            style="width: 100%"
            max-height="300"
            @row-click="handleRowClick"
            :row-class-name="getRowClassName"
          >
            <el-table-column label="接口名称" show-overflow-tooltip min-width="200">
              <template #default="scope">
                <el-link type="primary" :underline="false" @click.stop="handleApiClick(scope.row)">
                  {{ formatApiKey(scope.row.apiKey) }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="errorCount" label="错误次数" width="100">
              <template #default="scope">
                <el-tag type="danger">{{ scope.row.errorCount }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center;">
                <el-icon style="margin-right: 8px;"><TrendCharts /></el-icon>
                <span>接口统计详情
                  <el-input type="text" style="margin-left: 8px; width: 120px;" v-model="detailSize" @blur="handleApiClick(undefined)">
                    <template #append>条</template>
                  </el-input>
                </span>
                <span v-if="selectedApi" style="margin-left: 15px; color: #409EFF; font-weight: bold;">
                  {{ selectedApi }}
                </span>
              </div>
              <el-button v-if="selectedApi" type="text" @click="clearSelection">清除选择</el-button>
            </div>
          </template>
          <div v-if="selectedApi && apiLogList && apiLogList.length > 0">
            <div style="margin-bottom: 20px;">
              <el-descriptions :column="4" border>
                <el-descriptions-item label="总调用次数">
                  <el-tag type="info">{{ apiLogList.length }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="平均响应时长">
                  <el-tag :type="getTimeTagType(avgResponseTime)">
                    {{ formatTime(avgResponseTime) }}ms
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="最大响应时长">
                  <el-tag :type="getTimeTagType(maxResponseTime)">
                    {{ formatTime(maxResponseTime) }}ms
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="最小响应时长">
                  <el-tag :type="getTimeTagType(minResponseTime)">
                    {{ formatTime(minResponseTime) }}ms
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <div style="height: 400px;">
              <div ref="chartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </div>
          <div v-else-if="selectedApi && apiLogList && apiLogList.length === 0" style="text-align: center; padding: 40px; color: #909399;">
            <el-icon style="font-size: 48px; margin-bottom: 10px;"><DataAnalysis /></el-icon>
            <p>该接口暂无调用记录</p>
          </div>
          <div v-else style="text-align: center; padding: 40px; color: #909399;">
            <el-icon style="font-size: 48px; margin-bottom: 10px;"><DataAnalysis /></el-icon>
            <p>请点击上方接口名称查看详细统计信息</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { getSlowApiList, getFrequentApiList, getErrorApiList, getApiStatistics, cleanErrors } from '@/api/monitor/apiStatistics'
import { Timer, DataAnalysis, Warning, TrendCharts, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const queryParams = ref({
  includeSystem: false
})

const slowApiList = ref([])
const frequentApiList = ref([])
const errorApiList = ref([])
const selectedApi = ref(null)
const apiLogList = ref([])
const chartRef = ref(null)
const detailSize = ref(200)
let chartInstance = null
const showRecent100ForSlow = ref(false)
const frequentPeriod = ref(7) // '7days' 近7天, '30days' 近30天

/** 查询列表 */
function handleQuery() {
  getList()
}

/** 获取列表数据 */
function getList() {
  const params = {
    includeSystem: queryParams.value.includeSystem,
    limit: 20
  }

  // 获取严重耗时接口
  getSlowApiList(params).then(response => {
    slowApiList.value = response.data || []
  })

  // 获取频繁调用接口
  const frequentParams = {
    ...params,
    days: frequentPeriod.value
  }
  getFrequentApiList(frequentParams).then(response => {
    frequentApiList.value = response.data || []
  })

  // 获取异常接口
  getErrorApiList(params).then(response => {
    errorApiList.value = response.data || []
  })
}

/** 点击接口名称 */
function handleApiClick(row) {
  if (row){
    selectedApi.value = row.apiKey
  }
  if (!selectedApi.value){
    return
  }
  const param = {
    apiKey: selectedApi.value,
    limit: detailSize.value,
  };
  getApiDetail(param)
}

/** 获取接口详情 */
function getApiDetail(apiKey) {
  getApiStatistics(apiKey).then(response => {
    apiLogList.value = response.data || []
    nextTick(() => {
      initChart()
    })
  }).catch(() => {
    apiLogList.value = []
    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
  })
}

/** 清除选择 */
function clearSelection() {
  selectedApi.value = null
  apiLogList.value = []
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
}

/** 行点击事件 */
function handleRowClick(row) {
  handleApiClick(row)
}

/** 获取行类名 */
function getRowClassName({ row, rowIndex }) {
  return 'cursor-pointer'
}

/** 格式化时间 */
function formatTime(time) {
  if (time == null) return '0'
  return Math.round(time)
}

/** 格式化日期 */
function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

/** 根据响应时间获取标签类型 */
function getTimeTagType(time) {
  if (time == null) return 'info'
  const t = Number(time)
  if (t > 1000) return 'danger'
  if (t > 500) return 'warning'
  if (t > 200) return ''
  return 'success'
}

/** 格式化接口名称（去掉HTTP方法前缀） */
function formatApiKey(apiKey) {
  if (!apiKey) return ''
  // 去掉 "GET "、"POST " 等前缀
  const parts = apiKey.split('@')
  if (parts.length > 1) {
    return parts[0]
  }
  return apiKey
}

/** 获取显示的耗时（根据开关显示100次平均或总计平均） */
function getDisplayTime(row) {
  if (showRecent100ForSlow.value) {
    const recent100 = row.recent100AvgResponseTime
    if (recent100 != null) {
      return recent100
    }
  }
  return row.avgResponseTime || 0
}

/** 耗时类型切换事件 */
function handleSlowTimeTypeChange() {
  // 切换时不需要重新加载数据，因为数据中已包含recent100AvgResponseTime
}

/** 高频调用时间范围切换事件 */
function handleFrequentPeriodChange() {
  getList()
}

/** 清理错误记录 */
function handleCleanErrors() {
  ElMessageBox.confirm(
    '确定要清理所有异常响应记录吗？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    cleanErrors().then(() => {
      ElMessage.success('清理成功')
      getList()
    }).catch(() => {
      ElMessage.error('清理失败')
    })
  }).catch(() => {
    // 用户取消
  })
}

/** 计算平均响应时间 */
const avgResponseTime = computed(() => {
  if (!apiLogList.value || apiLogList.value.length === 0) return 0
  const sum = apiLogList.value.reduce((acc, log) => acc + (log.responseTime || 0), 0)
  return sum / apiLogList.value.length
})

/** 计算最大响应时间 */
const maxResponseTime = computed(() => {
  if (!apiLogList.value || apiLogList.value.length === 0) return 0
  return Math.max(...apiLogList.value.map(log => log.responseTime || 0))
})

/** 计算最小响应时间 */
const minResponseTime = computed(() => {
  if (!apiLogList.value || apiLogList.value.length === 0) return 0
  return Math.min(...apiLogList.value.map(log => log.responseTime || 0))
})

/** 初始化图表 */
function initChart() {
  if (!chartRef.value || !apiLogList.value || apiLogList.value.length === 0) {
    return
  }

  // 销毁旧图表
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 创建新图表
  chartInstance = echarts.init(chartRef.value, 'macarons')

  // 准备数据
  const times = apiLogList.value.map(log => {
    const date = new Date(log.createTime)
    return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
  }).reverse()

  const responseTimes = apiLogList.value.map(log => log.responseTime || 0).reverse()

  // 配置选项
  const option = {
    title: {
      text: '响应时间趋势图',
      left: 'center',
      textStyle: {
        fontSize: 16
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const param = params[0]
        return `${param.name}<br/>响应时间: ${param.value}ms`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times,
      axisLabel: {
        rotate: 45,
        interval: Math.floor(times.length / 10) || 0
      }
    },
    yAxis: {
      type: 'value',
      name: '响应时间(ms)',
      axisLabel: {
        formatter: '{value}ms'
      }
    },
    series: [
      {
        name: '响应时间',
        type: 'line',
        smooth: true,
        data: responseTimes,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        },
        markLine: {
          data: [
            { type: 'average', name: '平均值' }
          ]
        }
      }
    ]
  }

  chartInstance.setOption(option)

  // 响应式调整
  const resizeHandler = () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  }
  window.addEventListener('resize', resizeHandler)

  // 保存清理函数
  if (chartInstance._resizeHandler) {
    window.removeEventListener('resize', chartInstance._resizeHandler)
  }
  chartInstance._resizeHandler = resizeHandler
}

// 监听数据变化，更新图表
watch(() => apiLogList.value, () => {
  nextTick(() => {
    if (apiLogList.value && apiLogList.value.length > 0 && chartRef.value) {
      initChart()
    }
  })
}, { deep: true })

onMounted(() => {
  getList()
})

onUnmounted(() => {
  if (chartInstance) {
    if (chartInstance._resizeHandler) {
      window.removeEventListener('resize', chartInstance._resizeHandler)
    }
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.app-container {
  padding: 10px;
}

</style>
