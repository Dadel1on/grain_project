import { createRouter, createWebHistory } from 'vue-router'
import Welcome from '../views/Welcome.vue'
import Layout from '../components/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import Devices from '../views/Devices.vue'
import Alarms from '../views/Alarms.vue'
import History from '../views/History.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Welcome',
      component: Welcome,
      meta: { title: '系统首页' }
    },
    {
      path: '/admin',
      component: Layout,
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: Dashboard,
          meta: { title: '实时监控' }
        },
        {
          path: '/devices',
          name: 'Devices',
          component: Devices,
          meta: { title: '设备管理' }
        },
        {
          path: '/alarms',
          name: 'Alarms',
          component: Alarms,
          meta: { title: '预警管理' }
        },
        {
          path: '/history',
          name: 'History',
          component: History,
          meta: { title: '历史记录' }
        }
      ]
    },
    // 重定向未知路由到首页
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

export default router
