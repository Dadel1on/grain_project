<template>
  <div class="apple-welcome-standalone">
    <!-- 顶部导航栏 (Standalone) -->
    <nav class="standalone-nav">
      <div class="nav-content">
        <div class="nav-logo">
          <div class="logo-icon-small">
            <el-icon><Box /></el-icon>
          </div>
          <span>智慧粮仓</span>
        </div>
        <div class="nav-links">
          <el-button link @click="$router.push('/dashboard')">控制台</el-button>
          <el-button link>关于项目</el-button>
        </div>
      </div>
    </nav>

    <!-- 英雄区 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="light-text">基于Spring Boot的</span>
          <span class="gradient-text">粮库温湿度监控预警系统</span>
        </h1>
        <p class="hero-subtitle">
          融合物联网、实时通信与大数据分析技术，为现代化粮食储备提供高精度、智能化的全方位监控方案。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/dashboard')" class="apple-btn-primary">
            立即进入控制中心
          </el-button>
          <el-button size="large" class="apple-btn-secondary" @click="scrollToTechSection">
            查看技术细节
          </el-button>
        </div>
      </div>
      <div class="hero-visual">
        <img 
          src="../assets/hero.png" 
          alt="智慧粮仓" 
          class="hero-image"
        />
      </div>
    </section>

    <!-- 特性网格 -->
    <section class="features-section">
      <div class="feature-grid">
        <div v-for="(item, index) in features" :key="index" class="feature-card">
          <div class="feature-icon" :style="{ background: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 技术栈 -->
    <section id="tech-section" class="tech-section">
      <div class="tech-header">
        <h2>前沿技术栈</h2>
        <p>基于工业级标准构建，确保系统的高可用性与可扩展性</p>
      </div>
      <div class="tech-tags">
        <span v-for="tech in techStack" :key="tech" class="tech-tag">{{ tech }}</span>
      </div>
    </section>

    <!-- 底部 -->
    <footer class="welcome-footer">
      <div class="footer-content">
        <p>© 2026 智慧粮仓监测系统. 基于软件工程专业毕业设计标准实现。</p>
        <div class="footer-links">
          <span>Spring Boot 3</span>
          <span class="divider">|</span>
          <span>Vue 3</span>
          <span class="divider">|</span>
          <span>MQTT</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { 
  Box,
  Monitor, 
  Bell, 
  DataLine, 
  Cpu, 
  Connection, 
  Checked
} from '@element-plus/icons-vue'

const features = [
  { title: '秒级实时感知', desc: '采用分布式 MQTT 架构，毫秒级数据上报，实时掌控粮仓动态。', icon: Monitor, color: '#0071e3' },
  { title: '毫秒预警响应', desc: '基于 WebSocket 的全双工通信，异常指标瞬间送达，风险零延迟。', icon: Bell, color: '#ff3b30' },
  { title: '全维数据追溯', desc: '工业级时序数据存储，支持多维度历史回溯与智能趋势分析。', icon: DataLine, color: '#34c759' },
  { title: '边缘节点管理', desc: '支持海量传感器节点接入，具备完善的设备生命周期管理能力。', icon: Cpu, color: '#af52de' },
  { title: '高可靠性架构', desc: '内置断线自动重连与数据校验机制，确保恶劣环境下的稳定性。', icon: Connection, color: '#5856d6' },
  { title: '系统安全加固', desc: '精细化权限管理与全量操作审计日志，符合行业合规性标准。', icon: Checked, color: '#ff9500' }
]

const techStack = [
  'Spring Boot 3', 'MyBatis-Plus', 'MySQL 8.0', 'MQTT 5.0', 'WebSocket', 
  'Vue 3', 'Vite', 'TypeScript', 'Pinia', 'Element Plus', 'ECharts 5'
]

const scrollToTechSection = () => {
  const section = document.getElementById('tech-section')
  if (section) {
    section.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}
</script>

<style scoped>
.apple-welcome-standalone {
  min-height: 100vh;
  background-color: #ffffff;
  color: #1d1d1f;
  overflow-x: hidden;
}

/* Standalone Nav */
.standalone-nav {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
}

.nav-content {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 18px;
}

.logo-icon-small {
  width: 28px;
  height: 28px;
  background: #0071e3;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-btn {
  padding: 8px 18px !important;
  font-weight: 600 !important;
}

/* Hero Section */
.hero-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.hero-content {
  max-width: 640px;
  z-index: 2;
}

.hero-title {
  font-size: 64px;
  line-height: 1.1;
  font-weight: 700;
  letter-spacing: -2.5px;
  margin-bottom: 24px;
}

.gradient-text {
  background: linear-gradient(135deg, #0071e3 0%, #00d4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 20px;
  line-height: 1.5;
  color: #86868b;
  margin-bottom: 48px;
  max-width: 500px;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.apple-btn-primary {
  height: 52px !important;
  padding: 0 36px !important;
  font-size: 16px !important;
  border-radius: 26px !important;
}

.apple-btn-secondary {
  height: 52px !important;
  padding: 0 36px !important;
  font-size: 16px !important;
  border-radius: 26px !important;
  background: #f5f5f7 !important;
  border: none !important;
}

.hero-visual {
  position: absolute;
  right: 0px;
  top: 50%;
  transform: translateY(-50%);
  width: 500px;
  height: 500px;
  z-index: 1;
}

.hero-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
  animation: morph 8s ease-in-out infinite;
  box-shadow: 0 20px 40px rgba(0, 113, 227, 0.2);
}

@keyframes morph {
  0% { border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%; }
  50% { border-radius: 30% 60% 70% 40% / 50% 60% 30% 60%; }
  100% { border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%; }
}

/* Features */
.features-section {
  background-color: #f5f5f7;
  padding: 100px 24px;
}

.feature-grid {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.feature-card {
  background: #ffffff;
  padding: 40px;
  border-radius: 30px;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
}

.feature-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 24px; margin-bottom: 24px;
}

.feature-card h3 { font-size: 20px; font-weight: 600; margin-bottom: 12px; }
.feature-card p { font-size: 15px; line-height: 1.6; color: #86868b; }

/* Tech */
.tech-section {
  padding: 100px 24px;
  text-align: center;
}

.tech-header h2 { font-size: 32px; font-weight: 700; margin-bottom: 12px; }
.tech-header p { color: #86868b; margin-bottom: 40px; }

.tech-tags {
  display: flex; flex-wrap: wrap; justify-content: center; gap: 12px;
}

.tech-tag {
  padding: 10px 22px; background: #f5f5f7; border-radius: 100px;
  font-size: 14px; font-weight: 500;
}

/* Footer */
.welcome-footer {
  padding: 60px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  text-align: center;
}

.footer-content p { font-size: 14px; color: #86868b; margin-bottom: 16px; }
.footer-links { display: flex; justify-content: center; gap: 12px; color: #d2d2d7; font-size: 13px; }
.divider { color: #e5e5e7; }
</style>
