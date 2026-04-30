import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import router from '@/router'

interface UserInfo {
  id: number
  username: string
  nickname: string
  token: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)

  const token = computed(() => user.value?.token || '')

  const isLoggedIn = computed(() => !!user.value && !!user.value.token)

  function initAuth() {
    const stored = localStorage.getItem('auth_user')
    if (stored) {
      try {
        user.value = JSON.parse(stored)
      } catch {
        localStorage.removeItem('auth_user')
      }
    }
  }

  async function login(username: string, password: string) {
    const res = await axios.post('/api/auth/login', { username, password })
    if (res.data.code === 200) {
      user.value = res.data.data
      localStorage.setItem('auth_user', JSON.stringify(res.data.data))
      return true
    }
    return false
  }

  async function register(username: string, password: string, nickname: string) {
    const res = await axios.post('/api/auth/register', { username, password, nickname })
    return {
      success: res.data.code === 200,
      message: res.data.message || ''
    }
  }

  function logout() {
    user.value = null
    localStorage.removeItem('auth_user')
    router.push('/login')
  }

  initAuth()

  return {
    user,
    token,
    isLoggedIn,
    login,
    register,
    logout,
    initAuth
  }
})
