<template>
  <el-tabs style="margin: 10px" v-model="activeName" type="card">
    <el-tab-pane label="Druid"  name="first" key="1" >
      <div>
        <i-frame v-model:src="url"></i-frame>
      </div>
    </el-tab-pane>
    <el-tab-pane label="接口统计" name="second" key="2">
      <ApiStatistics />
    </el-tab-pane>
  </el-tabs>


</template>

<script setup>
import iFrame from '@/components/iFrame'
import ApiStatistics from '@/views/monitor/druid/api.vue'
import { ref } from 'vue'
const activeName=ref("second")

const url = ref(import.meta.env.VITE_APP_BASE_API + '/druid/login.html')
</script>

