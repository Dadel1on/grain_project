<template>
  <el-config-provider>
    <router-view v-slot="{ Component }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </el-config-provider>
</template>

<script setup lang="ts">
// Global configuration and styles
</script>

<style>
:root {
  --apple-bg: #f5f5f7;
  --apple-sidebar: #ffffff;
  --apple-text: #1d1d1f;
  --apple-text-secondary: #86868b;
  --apple-blue: #0071e3;
  --apple-card-bg: rgba(255, 255, 255, 0.8);
  --apple-blur: blur(20px);
}

body {
  margin: 0;
  padding: 0;
  background-color: var(--apple-bg);
  color: var(--apple-text);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}

/* Page Transitions */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Global Component Overrides for Apple Style */
.el-card {
  border-radius: 18px !important;
  border: none !important;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04) !important;
  background-color: var(--apple-card-bg) !important;
  backdrop-filter: var(--apple-blur);
  -webkit-backdrop-filter: var(--apple-blur);
}

.el-button {
  border-radius: 10px !important;
  font-weight: 500 !important;
  transition: all 0.2s !important;
}

.el-button--primary {
  background-color: var(--apple-blue) !important;
  border: none !important;
}

.el-button--primary:hover {
  opacity: 0.9;
  transform: scale(1.02);
}

.el-table {
  background-color: transparent !important;
}

.el-table th.el-table__cell {
  background-color: rgba(0, 0, 0, 0.02) !important;
  color: var(--apple-text-secondary) !important;
  font-weight: 600 !important;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.5px;
}

.el-table__row {
  background-color: transparent !important;
}
</style>
