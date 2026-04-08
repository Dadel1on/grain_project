<template>
  <div class="apple-page">
    <div class="page-header">
      <div class="header-text">
        <h2>历史监测数据溯源</h2>
        <p>多维检索与分析过往所有监测点的环境指标记录</p>
      </div>
    </div>

    <div class="apple-card-container search-box">
      <div class="search-form">
        <div class="form-item">
          <label>监测设备</label>
          <el-select v-model="searchForm.deviceId" placeholder="全部设备" clearable class="apple-select">
            <el-option v-for="d in devices" :key="d.deviceId" :label="d.deviceName" :value="d.deviceId" />
          </el-select>
        </div>
        <div class="form-item range-item">
          <label>时间跨度</label>
          <el-date-picker
            v-model="searchForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="apple-date-picker"
          />
        </div>
        <div class="form-actions">
          <el-button type="primary" class="apple-btn" @click="fetchHistory">
            <el-icon><Search /></el-icon> 执行检索
          </el-button>
          <el-button @click="resetSearch" class="apple-btn-secondary">重置</el-button>
        </div>
      </div>
    </div>

    <div class="apple-card-container">
      <el-table :data="historyData" class="apple-table" height="480">
        <el-table-column prop="deviceId" label="设备标识" width="120" />
        <el-table-column prop="temperature" label="温度" width="120">
          <template #default="{ row }">
            <div class="val-cell">
              <span class="dot" :style="{ background: getTempColor(row.temperature) }"></span>
              <span class="val">{{ row.temperature }}<span class="u">°C</span></span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="humidity" label="湿度" width="120">
          <template #default="{ row }">
            <div class="val-cell">
              <span class="dot" :style="{ background: getHumColor(row.humidity) }"></span>
              <span class="val">{{ row.humidity }}<span class="u">%</span></span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="collectTime" label="采集时间戳" min-width="200">
          <template #default="{ row }">
            <span class="time-text">{{ dayjs(row.collectTime).format('YYYY年MM月DD日 HH:mm:ss') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import dayjs from 'dayjs'
import { Search } from '@element-plus/icons-vue'

const devices = ref<any[]>([])
const historyData = ref<any[]>([])
const searchForm = reactive({
  deviceId: '',
  timeRange: [] as string[]
})

const fetchDevices = async () => {
  const res = await axios.get('/api/devices')
  devices.value = res.data.data
}

const fetchHistory = async () => {
  const params: any = {
    deviceId: searchForm.deviceId,
    startTime: searchForm.timeRange?.[0],
    endTime: searchForm.timeRange?.[1]
  }
  const res = await axios.get('/api/sensor-data', { params })
  historyData.value = res.data.data
}

const resetSearch = () => {
  searchForm.deviceId = ''
  searchForm.timeRange = []
  fetchHistory()
}

const getTempColor = (val: number) => {
  if (val > 30) return '#ff3b30'
  if (val < 10) return '#0071e3'
  return '#34c759'
}

const getHumColor = (val: number) => {
  if (val > 70) return '#ff9500'
  if (val < 30) return '#ff3b30'
  return '#34c759'
}

onMounted(() => {
  fetchDevices()
  fetchHistory()
})
</script>

<style scoped>
.apple-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.page-header p {
  color: var(--apple-text-secondary);
  font-size: 15px;
}

.apple-card-container {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  align-items: flex-end;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.apple-select {
  width: 200px;
}

.apple-date-picker {
  width: 380px !important;
}

.form-item label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-secondary);
}

.apple-select :deep(.el-input__wrapper),
.apple-date-picker :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #f5f5f7;
  border: none;
  box-shadow: none !important;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.apple-btn {
  border-radius: 12px !important;
  height: 40px !important;
  padding: 0 24px !important;
}

.apple-btn-secondary {
  border-radius: 12px !important;
  height: 40px !important;
  padding: 0 24px !important;
  border: 1px solid #e5e5e7 !important;
  background: transparent !important;
}

.val-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.val {
  font-weight: 600;
}

.u {
  font-size: 10px;
  color: var(--apple-text-secondary);
  margin-left: 2px;
}

.time-text {
  color: var(--apple-text-secondary);
  font-size: 13px;
}
</style>

