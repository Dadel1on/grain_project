<template>
  <div class="apple-dashboard">
    <!-- 全局大屏 -->
    <section class="big-screen-section">
      <div class="big-screen-header">
        <div class="title-group">
          <h2>全局态势大屏</h2>
          <p>展示所有在线传感器的实时指标变化</p>
        </div>
        <div class="big-screen-meta">
          <div class="meta-item">
            <span class="meta-label">数据源</span>
            <span class="meta-value">WebSocket + MySQL</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">更新时间</span>
            <span class="meta-value">{{ latestUpdateLabel }}</span>
          </div>
        </div>
      </div>

      <div class="big-screen-kpis">
        <div class="kpi-card">
          <span class="kpi-label">平均温度</span>
          <span class="kpi-value">{{ avgTemperature }}</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">平均湿度</span>
          <span class="kpi-value">{{ avgHumidity }}</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">在线传感器</span>
          <span class="kpi-value">{{ onlineDeviceCount }}</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">实时告警</span>
          <span class="kpi-value warning">{{ alarmCount }}</span>
        </div>
      </div>

      <div class="big-screen-body">
        <div class="big-screen-chart-card">
          <div class="big-screen-chart-header">
            <div>
              <h3>所有传感器指标变化</h3>
              <p>折线代表各设备最近采样趋势</p>
            </div>
            <div class="metric-tabs">
              <button
                class="metric-tab"
                :class="{ active: activeMetric === 'temperature' }"
                @click="activeMetric = 'temperature'"
              >
                温度
              </button>
              <button
                class="metric-tab"
                :class="{ active: activeMetric === 'humidity' }"
                @click="activeMetric = 'humidity'"
              >
                湿度
              </button>
            </div>
          </div>
          <div ref="globalChartRef" class="global-chart"></div>
        </div>

        <div class="big-screen-device-panel">
          <div class="panel-title">设备最新状态</div>
          <div class="mini-device-list">
            <div v-for="device in devices" :key="device.deviceId" class="mini-device-item">
              <div class="mini-device-top">
                <span class="mini-device-name">{{ device.deviceName }}</span>
                <span class="mini-device-status" :class="{ online: device.status === 1 }">
                  {{ device.status === 1 ? '在线' : '离线' }}
                </span>
              </div>
              <div class="mini-device-values">
                <span>温度 {{ latestByDevice[device.deviceId]?.temperature ?? '--' }}°C</span>
                <span>湿度 {{ latestByDevice[device.deviceId]?.humidity ?? '--' }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 顶部状态概览 -->
    <div class="stats-overview">
      <div class="stat-item">
        <div class="stat-label">监测点总数</div>
        <div class="stat-value">{{ devices.length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">在线设备</div>
        <div class="stat-value active">{{ devices.filter(d => d.status === 1).length }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">异常告警</div>
        <div class="stat-value warning">{{ alarmCount }}</div>
      </div>
    </div>

    <!-- 设备卡片网格 -->
    <div class="device-grid" v-loading="loading">
      <div v-for="device in devices" :key="device.deviceId" 
           class="apple-device-card" 
           :class="{ 
             'is-offline': device.status !== 1,
             'is-selected': selectedDeviceId === device.deviceId 
           }"
           @click="handleDeviceChange(device.deviceId)">
        <div class="card-top">
          <div class="device-icon">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="status-dot" :class="{ 'online': device.status === 1 }"></div>
        </div>
        <div class="device-info">
          <h3 class="device-name">{{ device.deviceName }}</h3>
          <p class="device-id">{{ device.deviceId }}</p>
        </div>
        <div class="device-data">
          <div class="data-group">
            <div class="data-val">{{ realTimeData[device.deviceId]?.temperature || '--' }}<span class="unit">°C</span></div>
            <div class="data-label">温度</div>
          </div>
          <div class="data-group">
            <div class="data-val">{{ realTimeData[device.deviceId]?.humidity || '--' }}<span class="unit">%</span></div>
            <div class="data-label">湿度</div>
          </div>
        </div>
        <div class="card-footer">
          <span class="update-time">{{ realTimeData[device.deviceId]?.timestamp?.split(' ')[1] || '等待数据...' }}</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section" v-loading="chartLoading">
      <div class="chart-header">
        <div class="title-group">
          <h2>实时数据趋势</h2>
          <p>正在监控: {{ currentDeviceName }}</p>
        </div>
      </div>
      <div class="chart-container">
        <div ref="chartRef" style="height: 360px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, computed, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import dayjs from 'dayjs'
import { Cpu, ArrowRight } from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'

const devices = ref<any[]>([])
const realTimeData = reactive<Record<string, any>>({})
const chartRef = ref<HTMLElement | null>(null)
const selectedDeviceId = ref('S001')
const alarmCount = ref(0)
const loading = ref(true)
const chartLoading = ref(false)
const globalChartRef = ref<HTMLElement | null>(null)
const activeMetric = ref<'temperature' | 'humidity'>('temperature')
const deviceHistoryMap = reactive<Record<string, any[]>>({})
const latestByDevice = reactive<Record<string, any>>({})
let myChart: echarts.ECharts | null = null
let globalChart: echarts.ECharts | null = null
let ws: WebSocket | null = null

const currentDeviceName = computed(() => {
  return devices.value.find(d => d.deviceId === selectedDeviceId.value)?.deviceName || '未选择'
})

const onlineDevices = computed(() => devices.value.filter(d => d.status === 1))
const onlineDeviceCount = computed(() => onlineDevices.value.length)

const avgTemperature = computed(() => {
  const values = onlineDevices.value
    .map(device => Number(latestByDevice[device.deviceId]?.temperature))
    .filter(value => Number.isFinite(value))
  if (values.length === 0) return '--'
  return `${(values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(1)}°C`
})

const avgHumidity = computed(() => {
  const values = onlineDevices.value
    .map(device => Number(latestByDevice[device.deviceId]?.humidity))
    .filter(value => Number.isFinite(value))
  if (values.length === 0) return '--'
  return `${(values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(1)}%`
})

const latestUpdateLabel = computed(() => {
  const timestamps = Object.values(latestByDevice)
    .map((item: any) => item?.timeLabel || item?.timestampLabel)
    .filter(Boolean)

  if (timestamps.length === 0) return '等待数据'

  return String(timestamps[timestamps.length - 1])
})

const ensureDeviceHistory = (deviceId: string) => {
  if (!deviceHistoryMap[deviceId]) {
    deviceHistoryMap[deviceId] = []
  }
  return deviceHistoryMap[deviceId]
}

const pushDeviceHistory = (deviceId: string, record: any) => {
  const history = ensureDeviceHistory(deviceId)
  history.push(record)
  if (history.length > 20) {
    history.shift()
  }
  latestByDevice[deviceId] = record
}

const renderGlobalChart = () => {
  if (!globalChart) return

  const devicesToShow = onlineDevices.value.length > 0 ? onlineDevices.value : devices.value
  const histories = devicesToShow.map(device => ensureDeviceHistory(device.deviceId).slice(-20))
  const maxLength = Math.max(0, ...histories.map(history => history.length))
  const categories = maxLength > 0
    ? Array.from({ length: maxLength }, (_, index) => `采样${index + 1}`)
    : ['暂无数据']

  const series = devicesToShow.map((device, index) => {
    const history = histories[index]
    const values = maxLength > 0
      ? Array.from({ length: maxLength }, (_, sampleIndex) => history[sampleIndex]?.[activeMetric.value] ?? null)
      : [latestByDevice[device.deviceId]?.[activeMetric.value] ?? null]

    return {
      name: device.deviceName,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      lineStyle: { width: 2 },
      data: values,
    }
  })

  globalChart.setOption({
    legend: {
      type: 'scroll',
      top: 6,
      left: 8,
      right: 8,
      textStyle: { color: '#86868b' }
    },
    xAxis: { data: categories },
    series,
    graphic: maxLength === 0 ? [{
      type: 'text',
      left: 'center',
      top: 'middle',
      style: {
        text: '暂无历史数据，等待模拟器推送',
        fill: '#86868b',
        fontSize: 14,
      }
    }] : [],
  })
}

// 获取初始历史数据填充图表
const fetchInitialChartData = async (deviceId: string) => {
  chartLoading.value = true
  try {
    const res = await axios.get('/api/sensor-data', {
      params: { deviceId, limit: 20 }
    })
    const history = res.data.data.reverse() // 按时间正序
    
    if (myChart) {
      const times = history.map((item: any) => dayjs(item.collectTime).format('HH:mm:ss'))
      const temps = history.map((item: any) => item.temperature)
      const hums = history.map((item: any) => item.humidity)
      
      myChart.setOption({
        xAxis: { data: times },
        series: [
          { data: temps },
          { data: hums }
        ]
      })
    }
  } catch (error) {
    console.error('获取历史数据失败', error)
  } finally {
    chartLoading.value = false
  }
}

const fetchAllDeviceHistory = async () => {
  if (devices.value.length === 0) return

  await Promise.all(
    devices.value.map(async (device) => {
      try {
        const res = await axios.get('/api/sensor-data', { params: { deviceId: device.deviceId, limit: 20 } })
        const history = res.data.data.reverse()
        history.forEach((item: any) => {
          const timeLabel = dayjs(item.collectTime).format('HH:mm:ss')
          pushDeviceHistory(device.deviceId, {
            rawTime: item.collectTime,
            timeLabel,
            temperature: item.temperature,
            humidity: item.humidity,
          })
        })
      } catch (error) {
        console.error(`获取设备 ${device.deviceId} 历史数据失败`, error)
      }
    })
  )

  renderGlobalChart()
}

// 监听设备切换
const handleDeviceChange = (val: string) => {
  selectedDeviceId.value = val
  fetchInitialChartData(val)
}

// 初始化图表 (Apple Style)
const initChart = () => {
  if (!chartRef.value) return
  myChart = echarts.init(chartRef.value)
  const option = {
    color: ['#0071e3', '#34c759'],
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.8)',
      backdropFilter: 'blur(10px)',
      borderWidth: 0,
      textStyle: { color: '#1d1d1f' },
      padding: 12,
      borderRadius: 12,
      boxShadow: '0 8px 24px rgba(0,0,0,0.1)'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true, top: '10%' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: [],
      axisLine: { lineStyle: { color: '#e5e5e5' } },
      axisLabel: { color: '#86868b' }
    },
    yAxis: [
      {
        type: 'value',
        name: '温度',
        splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } },
        axisLabel: { color: '#86868b' }
      },
      {
        type: 'value',
        name: '湿度',
        splitLine: { show: false },
        axisLabel: { color: '#86868b' }
      }
    ],
    series: [
      {
        name: '温度 (°C)',
        type: 'line',
        smooth: true,
        symbol: 'none',
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 113, 227, 0.2)' },
            { offset: 1, color: 'rgba(0, 113, 227, 0)' }
          ])
        },
        data: []
      },
      {
        name: '湿度 (%)',
        type: 'line',
        smooth: true,
        symbol: 'none',
        yAxisIndex: 1,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(52, 199, 89, 0.2)' },
            { offset: 1, color: 'rgba(52, 199, 89, 0)' }
          ])
        },
        data: []
      }
    ]
  }
  myChart.setOption(option)
}

const initGlobalChart = () => {
  if (!globalChartRef.value) return

  globalChart = echarts.init(globalChartRef.value)
  globalChart.setOption({
    color: ['#0071e3', '#34c759', '#ff9500', '#ff3b30', '#5856d6', '#00c7be', '#af52de', '#8e8e93'],
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderWidth: 0,
      textStyle: { color: '#1d1d1f' },
      borderRadius: 12,
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true, top: '12%' },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: [],
      axisLine: { lineStyle: { color: '#e5e5e5' } },
      axisLabel: { color: '#86868b' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } },
      axisLabel: { color: '#86868b' }
    },
    series: []
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    // 并行获取设备列表和初始图表数据
    const [devicesRes] = await Promise.all([
      axios.get('/api/devices'),
      fetchInitialChartData(selectedDeviceId.value)
    ])
    devices.value = devicesRes.data.data
    await nextTick()
    initGlobalChart()
    await fetchAllDeviceHistory()
  } catch (error) {
    console.error('数据加载失败', error)
  } finally {
    loading.value = false
  }
}

const connectWebSocket = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host === 'localhost:3000' ? 'localhost:8081' : window.location.host
  ws = new WebSocket(`${protocol}//${host}/ws/monitoring`)

  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    
    if (data.type === 'ALARM') {
      alarmCount.value++
      // 屏蔽报警弹窗
      // ElNotification({
      //   title: '环境预警',
      //   message: `${data.deviceId} 检测到数值异常`,
      //   type: 'warning',
      //   position: 'bottom-right'
      // })
      return
    }

    realTimeData[data.deviceId] = data
    pushDeviceHistory(data.deviceId, {
      rawTime: data.timestamp || dayjs().format('YYYY-MM-DD HH:mm:ss'),
      timeLabel: data.timestamp ? dayjs(data.timestamp).format('HH:mm:ss') : dayjs().format('HH:mm:ss'),
      temperature: data.temperature,
      humidity: data.humidity,
    })

    renderGlobalChart()

    if (data.deviceId === selectedDeviceId.value && myChart && !chartLoading.value) {
      const option = myChart.getOption() as any
      const time = dayjs().format('HH:mm:ss')
      
      option.xAxis[0].data.push(time)
      option.series[0].data.push(data.temperature)
      option.series[1].data.push(data.humidity)

      if (option.xAxis[0].data.length > 20) {
        option.xAxis[0].data.shift()
        option.series[0].data.shift()
        option.series[1].data.shift()
      }
      myChart.setOption(option)
    }
  }

  ws.onclose = () => setTimeout(connectWebSocket, 5000)
}

onMounted(() => {
  initChart()
  fetchData()
  connectWebSocket()
  window.addEventListener('resize', () => myChart?.resize())
})

onUnmounted(() => {
  ws?.close()
  myChart?.dispose()
  globalChart?.dispose()
})

watch(activeMetric, () => {
  renderGlobalChart()
})
</script>

<style scoped>
.apple-dashboard {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* Big Screen */
.big-screen-section {
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  color: var(--apple-text);
  border-radius: 28px;
  padding: 28px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.big-screen-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
  margin-bottom: 22px;
}

.big-screen-header .title-group h2 {
  color: var(--apple-text);
  margin-bottom: 6px;
}

.big-screen-header .title-group p,
.meta-label {
  color: var(--apple-text-secondary);
}

.big-screen-meta {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
}

.meta-item {
  min-width: 140px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #eef2f7;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.meta-label {
  display: block;
  font-size: 12px;
  margin-bottom: 4px;
}

.meta-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text);
}

.big-screen-kpis {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 22px;
}

.kpi-card {
  padding: 18px 20px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #eef2f7;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
}

.kpi-label {
  display: block;
  font-size: 13px;
  color: var(--apple-text-secondary);
  margin-bottom: 10px;
}

.kpi-value {
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.8px;
  color: var(--apple-text);
}

.kpi-value.warning {
  color: #ff9f0a;
}

.big-screen-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.big-screen-chart-card,
.big-screen-device-panel {
  border-radius: 24px;
  background: #ffffff;
  border: 1px solid #eef2f7;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
  padding: 20px;
}

.big-screen-chart-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.big-screen-chart-header h3,
.panel-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
}

.big-screen-chart-header p {
  margin: 0;
  color: var(--apple-text-secondary);
  font-size: 13px;
}

.metric-tabs {
  display: inline-flex;
  gap: 8px;
  padding: 6px;
  border-radius: 999px;
  background: #f5f7fa;
  border: 1px solid #e8eef6;
}

.metric-tab {
  border: 0;
  padding: 8px 14px;
  border-radius: 999px;
  background: transparent;
  color: var(--apple-text-secondary);
  cursor: pointer;
}

.metric-tab.active {
  background: var(--apple-blue);
  color: #ffffff;
  font-weight: 600;
}

.global-chart {
  height: 500px;
}

.mini-device-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.mini-device-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: #f9fbfe;
  border: 1px solid #eef2f7;
}

.mini-device-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.mini-device-name {
  font-weight: 600;
  color: var(--apple-text);
}

.mini-device-status {
  font-size: 12px;
  color: var(--apple-text-secondary);
}

.mini-device-status.online {
  color: #34c759;
}

.mini-device-values {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: var(--apple-text-secondary);
}

/* Stats Overview */
.stats-overview {
  display: flex;
  gap: 20px;
}

.stat-item {
  background: white;
  padding: 20px 30px;
  border-radius: 20px;
  flex: 1;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}

.stat-label {
  font-size: 13px;
  color: var(--apple-text-secondary);
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -1px;
}

.stat-value.active { color: var(--apple-blue); }
.stat-value.warning { color: #ff3b30; }

/* Device Grid */
.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.apple-device-card {
  background: white;
  padding: 24px;
  border-radius: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.apple-device-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.08);
}

.apple-device-card.is-selected {
  border-color: var(--apple-blue);
  background: rgba(0, 113, 227, 0.02);
  box-shadow: 0 8px 24px rgba(0, 113, 227, 0.1);
}

.apple-device-card.is-selected::after {
  content: '正在监控';
  position: absolute;
  top: 12px;
  right: -25px;
  background: var(--apple-blue);
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 30px;
  transform: rotate(45deg);
}

.apple-device-card.is-offline {
  opacity: 0.6;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.device-icon {
  width: 40px;
  height: 40px;
  background: #f5f5f7;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: var(--apple-text);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d1d1d6;
}

.status-dot.online {
  background: #34c759;
  box-shadow: 0 0 8px rgba(52, 199, 89, 0.5);
}

.device-info h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px;
}

.device-id {
  font-size: 13px;
  color: var(--apple-text-secondary);
  margin-bottom: 24px;
}

.device-data {
  display: flex;
  gap: 30px;
  margin-bottom: 24px;
}

.data-val {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.unit {
  font-size: 12px;
  font-weight: 500;
  margin-left: 2px;
}

.data-label {
  font-size: 12px;
  color: var(--apple-text-secondary);
  margin-top: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f5f5f7;
}

.update-time {
  font-size: 11px;
  color: var(--apple-text-secondary);
}

.arrow-icon {
  font-size: 14px;
  color: #c7c7cc;
}

/* Chart Section */
.chart-section {
  background: white;
  padding: 30px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.title-group h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 6px;
}

.title-group p {
  font-size: 14px;
  color: var(--apple-text-secondary);
}

.apple-select {
  width: 160px;
}

.apple-select :deep(.el-input__inner) {
  border-radius: 100px;
  background: #f5f5f7;
  border: none;
}

@media (max-width: 1200px) {
  .big-screen-kpis,
  .big-screen-body {
    grid-template-columns: 1fr 1fr;
  }

  .big-screen-body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .big-screen-header,
  .big-screen-chart-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .big-screen-kpis,
  .stats-overview {
    grid-template-columns: 1fr;
    display: grid;
  }

  .stats-overview {
    gap: 12px;
  }

  .global-chart {
    height: 360px;
  }
}
</style>

