<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- 左侧品牌区 -->
      <div class="auth-left">
        <div class="brand-content">
          <div class="brand-icon">🍽️</div>
          <h1 class="brand-title">校园饭搭子</h1>
          <p class="brand-desc">加入我们，开启校园美食社交之旅</p>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">📝</span>
              <span>30秒快速注册</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">👥</span>
              <span>发起或加入约饭组</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🤖</span>
              <span>AI 智能美食推荐</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔒</span>
              <span>安全可靠，隐私保护</span>
            </div>
          </div>
        </div>
        <div class="brand-footer">
          <span>上海理工大学 · 校园智能饭搭子系统</span>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="auth-right">
        <div class="form-wrapper">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>注册后即可使用全部功能</p>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" class="auth-form" label-position="top">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="给自己起个好听的名字"
                size="large"
                :prefix-icon="User"
                maxlength="20"
                show-word-limit
              />
            </el-form-item>
            <el-form-item prop="email">
              <el-input
                v-model="form.email"
                placeholder="输入邮箱地址"
                size="large"
                :prefix-icon="Message"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="设置密码（至少6位）"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="手机号（选填，用于重要通知）"
                size="large"
                :prefix-icon="Phone"
                maxlength="11"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                @click="handleRegister"
                :loading="loading"
                class="submit-btn"
              >
                注 册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span class="link-text">已有账号？</span>
            <router-link to="/login" class="link">立即登录</router-link>
          </div>

          <el-divider>
            <span class="divider-text">或者</span>
          </el-divider>

          <el-button size="large" class="guest-btn" @click="$router.push('/')">
            先看看，稍后注册
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { User, Message, Lock, Phone } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', email: '', password: '', phone: '' })
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为2-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.register(form)
    router.push('/')
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8eaf6 0%, #f3e5f5 50%, #fce4ec 100%);
  padding: 20px;
}

.auth-container {
  display: flex;
  width: 960px;
  min-height: 620px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 25px 60px rgba(0,0,0,0.12), 0 8px 20px rgba(0,0,0,0.06);
  overflow: hidden;
}

.auth-left {
  flex: 1;
  background: linear-gradient(160deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 50px 40px;
  position: relative;
  overflow: hidden;
}

.auth-left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -30%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255,255,255,0.03);
}

.auth-left::after {
  content: '';
  position: absolute;
  bottom: -20%;
  left: -20%;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255,255,255,0.03);
}

.brand-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.brand-icon {
  font-size: 56px;
  margin-bottom: 16px;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.3));
}

.brand-title {
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.brand-desc {
  color: rgba(255,255,255,0.7);
  font-size: 14px;
  margin: 0 0 36px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255,255,255,0.85);
  font-size: 14px;
}

.feature-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.brand-footer {
  position: absolute;
  bottom: 24px;
  color: rgba(255,255,255,0.4);
  font-size: 12px;
}

.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  text-align: center;
  margin-bottom: 24px;
}

.form-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.auth-form {
  margin-bottom: 8px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: box-shadow 0.2s;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2) inset;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.submit-btn {
  width: 100%;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  height: 44px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #5a6fd6, #6a4190);
}

.form-footer {
  text-align: center;
  margin-top: 16px;
}

.link-text {
  color: #999;
  font-size: 14px;
}

.link {
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}

.link:hover {
  color: #764ba2;
}

.divider-text {
  color: #ccc;
  font-size: 13px;
}

.guest-btn {
  width: 100%;
  border-radius: 10px;
  border: 2px dashed #e4e7ed;
  color: #999;
  height: 44px;
}

.guest-btn:hover {
  border-color: #667eea;
  color: #667eea;
}
</style>
