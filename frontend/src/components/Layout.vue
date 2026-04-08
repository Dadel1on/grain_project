<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="apple-sidebar">
      <div class="logo-container" @click="goToHome" style="cursor: pointer;">
        <div class="logo-icon">
          <el-icon><Box /></el-icon>
        </div>
        <span class="logo-text">智慧粮仓</span>
      </div>
      <el-menu
        router
        :default-active="$route.path"
        class="apple-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>实时监控</span>
        </el-menu-item>
        <el-menu-item index="/devices">
          <el-icon><Cpu /></el-icon>
          <span>设备管理</span>
        </el-menu-item>
        <el-menu-item index="/alarms">
          <el-icon><Bell /></el-icon>
          <span>预警管理</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><DataLine /></el-icon>
          <span>历史记录</span>
        </el-menu-item>
        <el-menu-item index="/">
          <el-icon><Back /></el-icon>
          <span>返回首页</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <div class="version-tag">v1.0.0 Stable</div>
      </div>
    </el-aside>
    <el-container class="main-container">
      <el-header class="apple-header">
        <div class="header-content">
          <div class="page-title">{{ $route.meta.title || '控制台' }}</div>
          <div class="header-right">
            <el-button circle><el-icon><Search /></el-icon></el-button>
            
            <!-- 报警收件箱 -->
            <el-dropdown trigger="click" class="inbox-dropdown">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="inbox-badge">
                <el-button circle><el-icon><Bell /></el-icon></el-button>
              </el-badge>
              <template #dropdown>
                <div class="inbox-panel">
                  <div class="inbox-header">
                    <span>报警收件箱</span>
                    <el-button v-if="unreadCount > 0" type="primary" round size="small" @click="handleAllRead" style="font-weight: 600;">全部忽略</el-button>
                  </div>
                  <el-scrollbar max-height="300px">
                    <div v-if="recentAlarms.length === 0" class="inbox-empty">
                      暂无未处理预警
                    </div>
                    <div v-for="alarm in recentAlarms" :key="alarm.id" class="inbox-item" @click="goToAlarms">
                      <div class="item-icon" :class="alarm.alarmType.includes('HIGH') ? 'high' : 'low'">
                        <el-icon><Warning /></el-icon>
                      </div>
                      <div class="item-body">
                        <div class="item-title">{{ alarm.deviceId }} {{ alarmTypeMap[alarm.alarmType] }}</div>
                        <div class="item-desc">当前值: {{ alarm.alarmValue }} (阈值: {{ alarm.thresholdValue }})</div>
                        <div class="item-time">{{ dayjs(alarm.createTime).fromNow() }}</div>
                      </div>
                    </div>
                  </el-scrollbar>
                  <div class="inbox-footer" @click="goToAlarms">
                    查看全部预警
                  </div>
                </div>
              </template>
            </el-dropdown>

            <el-button circle><el-icon><QuestionFilled /></el-icon></el-button>
          </div>
        </div>
      </el-header>
      <el-main class="apple-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { 
  Search, 
  QuestionFilled, 
  Box, 
  Monitor, 
  Cpu, 
  Bell, 
  DataLine,
  Back,
  Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const route = useRoute()

const unreadCount = ref(0)
const recentAlarms = ref<any[]>([])
let ws: WebSocket | null = null

const alarmTypeMap: Record<string, string> = {
  'TEMPERATURE_HIGH': '温度过高',
  'TEMPERATURE_LOW': '温度过低',
  'HUMIDITY_HIGH': '湿度过高',
  'HUMIDITY_LOW': '湿度过低'
}

const fetchUnreadAlarms = async () => {
  try {
    const res = await axios.get('/api/alarm-logs', { params: { status: 0 } })
    recentAlarms.value = res.data.data.slice(0, 5) // 只显示最近5条
    unreadCount.value = res.data.data.length
  } catch (error) {
    console.error('获取未读报警失败', error)
  }
}

const handleAllRead = async () => {
  try {
    await axios.put('/api/alarm-logs/handle-all')
    ElMessage.success('已全部忽略')
    fetchUnreadAlarms()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToAlarms = () => {
  router.push('/alarms')
}

const goToHome = () => {
  router.push('/')
}

const connectWebSocket = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host === 'localhost:3000' ? 'localhost:8081' : window.location.host
  ws = new WebSocket(`${protocol}//${host}/ws/monitoring`)

  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (data.type === 'ALARM') {
      fetchUnreadAlarms() // 收到新报警时重新获取
    }
  }

  ws.onclose = () => setTimeout(connectWebSocket, 5000)
}

onMounted(() => {
  fetchUnreadAlarms()
  connectWebSocket()
})

onUnmounted(() => {
  ws?.close()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* Sidebar Styling */
.apple-sidebar {
  background-color: var(--apple-sidebar);
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  padding: 20px 0;
  z-index: 10;
}

.logo-container {
  display: flex;
  align-items: center;
  padding: 0 24px 30px;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--apple-blue);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.apple-menu {
  border: none !important;
  flex: 1;
}

.apple-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 4px 12px;
  border-radius: 10px;
  color: var(--apple-text-secondary);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.apple-menu :deep(.el-menu-item:hover) {
  background-color: rgba(0, 0, 0, 0.04) !important;
  color: var(--apple-text);
}

.apple-menu :deep(.el-menu-item.is-active) {
  background-color: var(--apple-blue) !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(0, 113, 227, 0.3);
}

.apple-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 12px;
}

.sidebar-footer {
  padding: 20px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.version-tag {
  font-size: 12px;
  color: var(--apple-text-secondary);
  background: rgba(0, 0, 0, 0.05);
  padding: 4px 8px;
  border-radius: 6px;
  display: inline-block;
}

/* Header Styling */
.apple-header {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: var(--apple-blur);
  -webkit-backdrop-filter: var(--apple-blur);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  height: 64px !important;
  display: flex;
  align-items: center;
  padding: 0 30px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 16px;
  border-left: 1px solid rgba(0, 0, 0, 0.1);
}

.username {
  font-size: 14px;
  font-weight: 500;
}

/* Main Content Area */
.apple-main {
  padding: 30px !important;
  background-color: var(--apple-bg);
}

/* Inbox Styling */
.inbox-panel {
  width: 320px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
}

.inbox-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f7;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
}

.inbox-empty {
  padding: 40px 0;
  text-align: center;
  color: var(--apple-text-secondary);
  font-size: 14px;
}

.inbox-item {
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.inbox-item:hover {
  background: #f5f5f7;
}

.item-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.item-icon.high { background: rgba(255, 59, 48, 0.1); color: #ff3b30; }
.item-icon.low { background: rgba(255, 149, 0, 0.1); color: #ff9500; }

.item-body { flex: 1; }
.item-title { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.item-desc { font-size: 12px; color: var(--apple-text-secondary); margin-bottom: 4px; }
.item-time { font-size: 11px; color: #c7c7cc; }

.inbox-footer {
  padding: 14px;
  text-align: center;
  border-top: 1px solid #f5f5f7;
  font-size: 13px;
  color: var(--apple-blue);
  font-weight: 500;
  cursor: pointer;
}

.inbox-footer:hover { background: #f5f5f7; }

.inbox-badge :deep(.el-badge__content) {
  top: 10px;
  right: 10px;
}
</style>
