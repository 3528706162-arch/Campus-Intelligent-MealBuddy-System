<template>
  <div class="layout">
    <el-container>
      <el-aside width="220px" class="sidebar">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-text">🍽 校园饭搭子</span>
        </div>

        <el-menu
          :default-active="currentRoute"
          router
          class="side-menu"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>约饭广场</span>
          </el-menu-item>

          <el-menu-item index="/create-group" v-if="userStore.isLoggedIn">
            <el-icon><Plus /></el-icon>
            <span>发起约饭</span>
          </el-menu-item>

          <el-menu-item index="/my-groups" v-if="userStore.isLoggedIn">
            <el-icon><List /></el-icon>
            <span>我的约饭</span>
          </el-menu-item>

          <el-menu-item index="/ai-assistant" v-if="userStore.isLoggedIn">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI 美食助手</span>
          </el-menu-item>

          <el-menu-item index="/profile" v-if="userStore.isLoggedIn">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>

          <el-menu-item index="/notifications" v-if="userStore.isLoggedIn">
            <el-icon><Bell /></el-icon>
            <span>消息通知</span>
            <el-badge :value="userStore.unreadCount" :hidden="!userStore.unreadCount" style="margin-left:auto" />
          </el-menu-item>

          <el-menu-item index="/admin" v-if="userStore.isAdmin">
            <el-icon><Setting /></el-icon>
            <span>管理后台</span>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <template v-if="userStore.isLoggedIn">
            <div class="user-brief" @click="$router.push('/profile')">
              <el-avatar :size="36" :src="userStore.user?.avatarUrl">{{ userStore.user?.username?.charAt(0) }}</el-avatar>
              <div class="user-text">
                <div class="user-name">
                  {{ userStore.user?.username }}
                  <el-tag :type="userStore.roleTagType" size="small" style="margin-left:4px;font-size:10px">{{ userStore.roleText }}</el-tag>
                </div>
                <div class="user-credit">信用 {{ userStore.user?.creditScore }}</div>
              </div>
            </div>
            <el-button text size="small" @click="handleLogout" style="width:100%;margin-top:4px">退出登录</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')" style="width:100%">登录</el-button>
            <el-button @click="$router.push('/register')" style="width:100%;margin-top:8px">注册</el-button>
          </template>
        </div>
      </el-aside>

      <el-container>
        <el-header class="main-header">
          <div class="header-breadcrumb">
            <el-breadcrumb>
              <el-breadcrumb-item :to="{path:'/'}">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-actions">
            <el-tooltip content="AI 助手" v-if="userStore.isLoggedIn">
              <el-button circle @click="$router.push('/ai-assistant')">
                <el-icon><ChatDotRound /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { notificationAPI } from '../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentRoute = computed(() => route.path)

const pageTitle = computed(() => {
  const map = {
    '/': '',
    '/create-group': '发起约饭',
    '/my-groups': '我的约饭',
    '/ai-assistant': 'AI 美食助手',
    '/profile': '个人中心',
    '/notifications': '消息通知',
    '/admin': '管理后台'
  }
  if (route.path.startsWith('/group/')) return '约饭详情'
  return map[route.path] || ''
})

async function fetchUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await notificationAPI.unreadCount()
    userStore.unreadCount = res.data
  } catch { /* ignore */ }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchUnread()
  if (userStore.isLoggedIn) userStore.fetchMyGroupCount()
  setInterval(fetchUnread, 30000)
})
</script>

<style scoped>
.layout {
  height: 100vh;
}
.sidebar {
  background: #1d1e2c;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.logo {
  padding: 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.side-menu {
  flex: 1;
  border-right: none;
  background: transparent;
}
.side-menu .el-menu-item {
  color: #a8abb2;
}
.side-menu .el-menu-item:hover,
.side-menu .el-menu-item.is-active {
  background: rgba(255,255,255,0.08);
  color: #fff;
}
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.user-brief {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.user-brief:hover {
  background: rgba(255,255,255,0.06);
}
.user-text {
  display: flex;
  flex-direction: column;
}
.user-name {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}
.user-credit {
  color: #a8abb2;
  font-size: 12px;
}
.main-header {
  height: 52px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
}
.main-content {
  background: #f5f7fa;
  min-height: calc(100vh - 52px);
  padding: 20px 24px;
}
</style>
