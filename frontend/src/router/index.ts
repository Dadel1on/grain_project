import { createRouter, createWebHistory } from 'vue-router'
import Welcome from '../views/Welcome.vue'
import Layout from '../components/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import Devices from '../views/Devices.vue'
import Alarms from '../views/Alarms.vue'
import History from '../views/History.vue'
import Login from '../views/Login.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { title: '登录', requiresAuth: false }
    },
    {
      path: '/',
      name: 'Welcome',
      component: Welcome,
      meta: { title: '系统首页', requiresAuth: false }
    },
    {
      path: '/admin',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: Dashboard,
          meta: { title: '实时监控', requiresAuth: true }
        },
        {
          path: '/devices',
          name: 'Devices',
          component: Devices,
          meta: { title: '设备管理', requiresAuth: true }
        },
        {
          path: '/alarms',
          name: 'Alarms',
          component: Alarms,
          meta: { title: '预警管理', requiresAuth: true }
        },
        {
          path: '/history',
          name: 'History',
          component: History,
          meta: { title: '历史记录', requiresAuth: true }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('auth_user')

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }

  next()
})

export default router
