<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <el-icon><Box /></el-icon>
        </div>
        <h1>智慧粮仓</h1>
        <p>粮库温湿度监控预警系统</p>
      </div>

      <el-form
        v-if="activeTab === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="login-form"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="昵称"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-switch">
        <el-link 
          :type="activeTab === 'login' ? 'primary' : 'info'" 
          :underline="false" 
          @click="switchTab('login')" 
          :class="{ active: activeTab === 'login' }"
        >
          登录
        </el-link>
        <span class="separator">/</span>
        <el-link 
          :type="activeTab === 'register' ? 'primary' : 'info'" 
          :underline="false" 
          @click="switchTab('register')" 
          :class="{ active: activeTab === 'register' }"
        >
          注册
        </el-link>
      </div>

      <div class="login-tip">
        <span>初始账号：admin / admin123</span>
      </div>
    </div>

    <footer class="login-footer">
      <p>© 2026 智慧粮仓监测系统</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref()
const registerFormRef = ref()
const loading = ref(false)
const activeTab = ref<'login' | 'register'>('login')

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function switchTab(tab: 'login' | 'register') {
  activeTab.value = tab
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const success = await authStore.login(loginForm.username, loginForm.password)
    if (success) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error('用户名或密码错误')
    }
  } catch {
    ElMessage.error('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const result = await authStore.register(
      registerForm.username,
      registerForm.password,
      registerForm.nickname
    )

    if (result.success) {
      ElMessage.success('注册成功，请登录')
      loginForm.username = registerForm.username
      loginForm.password = ''
      switchTab('login')
      return
    }

    ElMessage.error(result.message || '注册失败')
  } catch (error: any) {
    const message = error?.response?.data?.message || error?.message || '注册失败，请检查网络连接'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: var(--apple-bg, #f5f5f7);
  background-image: radial-gradient(circle at 15% 50%, rgba(0, 113, 227, 0.04), transparent 25%),
                    radial-gradient(circle at 85% 30%, rgba(0, 113, 227, 0.04), transparent 25%);
}

.login-card {
  width: 400px;
  padding: 48px 40px;
  background: var(--apple-card-bg, rgba(255, 255, 255, 0.8));
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04);
  backdrop-filter: var(--apple-blur, blur(20px));
  -webkit-backdrop-filter: var(--apple-blur, blur(20px));
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-header .logo-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: var(--apple-blue, #0071e3);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
  box-shadow: 0 8px 16px rgba(0, 113, 227, 0.2);
}

.login-header h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  color: var(--apple-text, #1d1d1f);
  letter-spacing: 0.5px;
}

.login-header p {
  margin: 10px 0 0;
  font-size: 14px;
  color: var(--apple-text-secondary, #86868b);
}

.login-form {
  margin-top: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05) inset;
  background-color: rgba(255, 255, 255, 0.6);
  transition: all 0.2s ease;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--apple-blue, #0071e3) inset, 0 0 0 3px rgba(0, 113, 227, 0.1);
  background-color: #fff;
}

.auth-switch {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  font-size: 14px;
}

.auth-switch .el-link {
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 4px 8px;
}

.auth-switch .el-link.active {
  font-weight: 600;
  transform: scale(1.05);
}

.auth-switch .separator {
  color: #d2d2d7;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  height: 48px;
  border-radius: 12px !important;
  margin-top: 8px;
}

.login-tip {
  text-align: center;
  font-size: 13px;
  color: var(--apple-text-secondary, #86868b);
  margin-top: 20px;
  opacity: 0.8;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  color: var(--apple-text-secondary, #86868b);
  font-size: 13px;
}
</style>
