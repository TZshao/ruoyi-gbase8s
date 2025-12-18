<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>权限SQL测试</span>
      </div>
    </template>
    <el-form :model="form" label-width="120px">
      <el-form-item label="数据权限范围">
        <el-select v-model="form.dataScope" placeholder="请选择数据权限范围" style="width: 100%"  @change="handleChange">
          <el-option label="全部数据权限" value="1" />
          <el-option label="自定数据权限" value="2" />
          <el-option label="本部门数据权限" value="3" />
          <el-option label="本部门及以下数据权限" value="4" />
          <el-option label="仅本人数据权限" value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="字段组">
        <el-select v-model="form.fieldGroup" placeholder="请选择字段组" style="width: 100%">
          <el-option label="PASS" value="PASS" />
          <el-option label="REJECT" value="REJECT" />
          <el-option label="SYS_DEPT_CODE" value="SYS_DEPT_CODE" />
          <el-option label="PROJ_DEPT" value="PROJ_DEPT" />
          <el-option label="PROJ_SELF" value="PROJ_SELF" />
        </el-select>
      </el-form-item>
      <el-form-item label="部门代码">
        <el-input
          v-model="deptCodesInput"
          type="textarea"
          :rows="3"
          placeholder="请输入部门代码，多个用逗号分隔"
          @input="handleDeptCodesInput"
          :disabled="form.dataScope !== '2'"
        />
        <div style="margin-top: 8px; font-size: 12px; color: #909399;">
          已输入：{{ form.deptCodes.length }} 个部门代码
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleTest" :loading="loading">测试</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">响应结果</el-divider>
      <div class="code-display">
        <highlightjs language="sql" :code="result" />
      </div>
  </el-card>
</template>

<script setup name="TestAuthExample">
import { testAuthSql } from "@/api/test/auth"

const form = reactive({
  dataScope: "2",
  fieldGroup: "SYS_DEPT_CODE",
  deptCodes: []
})

const deptCodesInput = ref("")
const result = ref("")
const loading = ref(false)

// 处理部门代码输入
function handleDeptCodesInput(value) {
  if (!value) {
    form.deptCodes = []
    return
  }
  form.deptCodes = value.split(',').map(code => code.trim()).filter(code => code)
}

// 处理字段组变化
function handleChange(value) {
  if(value === "5"){
    form.deptCodes = ["userName"]
    deptCodesInput.value = "userName" // 同步更新输入框显示
    form.fieldGroup = "PROJ_SELF"
  } else if(value === "2"){
    // 切换到其他字段组时，清空部门代码
    form.deptCodes = ['10001','10002','10003']
    deptCodesInput.value = "10001,10002,10003"
  }else{
    form.deptCodes = []
    deptCodesInput.value = ""
  }
}
// 测试接口
function handleTest() {
  if (!form.dataScope || !form.fieldGroup) {
    proxy.$modal.msgWarning("请填写完整信息")
    return
  }
  loading.value = true
  testAuthSql(form.dataScope, form.fieldGroup, form.deptCodes).then(res => {
    if(res){
      result.value = "select * from ? where yourCnd AND (" + '\n' + res + '\n' + ")";
    }else{
      result.value = "无权限SQL"
    }
    loading.value = false
    proxy.$modal.msgSuccess("测试成功")
  }).catch(err => {
    result.value = `错误：${err.msg || err.message || "请求失败"}`
    loading.value = false
    proxy.$modal.msgError("测试失败")
  })
}

// 重置
function handleReset() {
  form.dataScope = "2"
  form.fieldGroup = "SYS_DEPT_CODE"
  form.deptCodes = []
  deptCodesInput.value = ""
  result.value = ""
}

// 复制结果
function copyResult() {
  if (!result.value) {
    proxy.$modal.msgWarning("没有可复制的内容")
    return
  }
  navigator.clipboard.writeText(result.value).then(() => {
    proxy.$modal.msgSuccess("复制成功")
  }).catch(() => {
    proxy.$modal.msgError("复制失败")
  })
}

const { proxy } = getCurrentInstance()
</script>

<style scoped>
.box-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.box-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.result-container {
  margin-top: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.code-display {
  border-radius: 4px;
  overflow: hidden;
}

.code-display :deep(.hljs) {
  padding: 15px;
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  background: #0d1117 !important;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol';
}

.empty-result {
  margin-top: 20px;
}
</style>
