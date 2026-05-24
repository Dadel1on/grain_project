<template>
  <div class="apple-page">
    <div class="page-header">
      <div class="header-text">
        <h2>智能预警中心</h2>
        <p>实时监控异常环境波动，配置多维告警阈值</p>
      </div>
    </div>

    <div class="alarm-content">
      <div class="alarm-main">
        <div class="apple-card-container">
          <div class="card-toolbar">
            <span class="toolbar-title">报警日志回顾</span>
            <div class="toolbar-actions">
              <el-button v-if="alarmStatus === '0' && alarmLogs.length > 0" 
                         type="primary" link @click="handleAllAlarms" class="all-read-btn">
                全部标记已读
              </el-button>
              <el-radio-group v-model="alarmStatus" @change="fetchAlarmLogs" class="apple-radio-group">
                <el-radio-button label="0">待处理</el-radio-button>
                <el-radio-button label="1">历史记录</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <el-table :data="alarmLogs" class="apple-table" height="520">
            <el-table-column prop="deviceId" label="设备" width="100" />
            <el-table-column prop="alarmType" label="类型" width="160">
              <template #default="{ row }">
                <span class="apple-alarm-tag" :class="(row.alarmType || '').includes('HIGH') ? 'high' : 'low'">
                  {{ alarmTypeMap[row.alarmType] || row.alarmType }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="alarmValue" label="实时值" width="100">
              <template #default="{ row }">
                <span class="value-text">{{ row.alarmValue }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="thresholdValue" label="阈值" width="100" />
            <el-table-column prop="createTime" label="时间" min-width="160">
              <template #default="{ row }">
                {{ dayjs(row.createTime).format('MM-DD HH:mm:ss') }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" v-if="alarmStatus === '0'">
              <template #default="{ row }">
                <el-button type="primary" class="apple-table-btn" @click="handleAlarm(row)">标记已读</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="alarm-side">
        <div class="apple-card-container compact">
          <div class="card-toolbar">
            <span class="toolbar-title">阈值配置</span>
          </div>
          <div class="config-list">
            <div v-for="conf in alarmConfigs" :key="conf.id" class="config-item" @click="editConfig(conf)">
              <div class="conf-info">
                <span class="conf-id">{{ conf.deviceId }}</span>
                <span class="conf-name">配置参数</span>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="configVisible" title="告警阈值设定" width="400px" class="apple-dialog">
      <el-form :model="currentConfig" label-position="top">
        <div class="form-section-title">温度范围 (℃)</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上限">
              <el-input-number v-model="currentConfig.maxTemp" :precision="1" :step="0.5" class="apple-number-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下限">
              <el-input-number v-model="currentConfig.minTemp" :precision="1" :step="0.5" class="apple-number-input" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <div class="form-section-title">湿度范围 (%)</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上限">
              <el-input-number v-model="currentConfig.maxHum" :precision="1" :step="1" class="apple-number-input" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下限">
              <el-input-number v-model="currentConfig.minHum" :precision="1" :step="1" class="apple-number-input" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="configVisible = false" class="apple-btn-text">取消</el-button>
          <el-button type="primary" @click="saveConfig" class="apple-btn">同步配置</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import dayjs from 'dayjs'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const alarmLogs = ref<any[]>([])
const alarmConfigs = ref<any[]>([])
const alarmStatus = ref('0')
const configVisible = ref(false)
const currentConfig = reactive<any>({})
const deviceIds = ref<Set<string>>(new Set())

const statusOptions = [
  { label: '待处理', value: '0' },
  { label: '历史', value: '1' }
]

const alarmTypeMap: Record<string, string> = {
  'TEMPERATURE_HIGH': '温度异常偏高',
  'TEMPERATURE_LOW': '温度异常偏低',
  'HUMIDITY_HIGH': '湿度异常偏高',
  'HUMIDITY_LOW': '湿度异常偏低'
}

const fetchDevices = async () => {
  const res = await axios.get('/api/devices')
  deviceIds.value = new Set(res.data.data.map((d: any) => d.deviceId))
}

const fetchAlarmLogs = async () => {
  const res = await axios.get('/api/alarm-logs', { params: { status: alarmStatus.value } })
  alarmLogs.value = res.data.data.filter((log: any) => deviceIds.value.has(log.deviceId))
}

const fetchConfigs = async () => {
  const res = await axios.get('/api/alarm-configs')
  alarmConfigs.value = res.data.data.filter((c: any) => deviceIds.value.has(c.deviceId))
}

const handleAlarm = async (row: any) => {
  await axios.put(`/api/alarm-logs/${row.id}/handle`)
  ElMessage.success('已标记为已读')
  fetchAlarmLogs()
}

const handleAllAlarms = async () => {
  await axios.put('/api/alarm-logs/handle-all')
  ElMessage.success('全部已读')
  fetchAlarmLogs()
}

const editConfig = (row: any) => {
  Object.assign(currentConfig, row)
  configVisible.value = true
}

const saveConfig = async () => {
  await axios.put('/api/alarm-configs', currentConfig)
  ElMessage.success('报警阈值已更新')
  configVisible.value = false
  fetchConfigs()
}

onMounted(async () => {
  await fetchDevices()
  fetchAlarmLogs()
  fetchConfigs()
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

.alarm-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.apple-card-container {
  background: white;
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
}

.card-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.apple-table-btn {
  font-size: 12px;
  font-weight: 600;
  height: 28px !important;
  padding: 0 12px !important;
  border-radius: 8px !important;
  background: rgba(0, 113, 227, 0.08) !important;
  border: none !important;
  color: var(--apple-blue) !important;
  transition: all 0.2s ease !important;
}

.apple-table-btn:hover {
  background: var(--apple-blue) !important;
  color: white !important;
  transform: translateY(-1px);
}

.all-read-btn {
  font-size: 12px;
  font-weight: 600;
  background: rgba(0, 113, 227, 0.08) !important;
  padding: 6px 14px !important;
  border-radius: 100px !important;
  transition: all 0.2s ease !important;
  border: none !important;
}

.all-read-btn:hover {
  background: rgba(0, 113, 227, 0.15) !important;
  transform: scale(1.02);
}

.all-read-btn:active {
  transform: scale(0.98);
}

.toolbar-title {
  font-size: 17px;
  font-weight: 600;
}

.apple-alarm-tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.apple-alarm-tag.high {
  background: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.apple-alarm-tag.low {
  background: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.value-text {
  font-weight: 700;
  font-family: 'SF Mono', 'Courier New', monospace;
}

/* Config List */
.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f5f7;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.config-item:hover {
  background: #e5e5e7;
}

.conf-id {
  display: block;
  font-weight: 600;
  font-size: 15px;
}

.conf-name {
  font-size: 12px;
  color: var(--apple-text-secondary);
}

.form-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-secondary);
  margin: 10px 0 15px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.apple-number-input :deep(.el-input-number__increase),
.apple-number-input :deep(.el-input-number__decrease) {
  background: #f5f5f7;
  border: none;
}

.apple-number-input :deep(.el-input__inner) {
  background: #f5f5f7;
  border: none;
  border-radius: 8px;
}
</style>

