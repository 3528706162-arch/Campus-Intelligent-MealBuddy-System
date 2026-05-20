<template>
  <div class="notifications">
    <div style="display:flex;justify-content:space-between;align-items:center">
      <h3>消息通知</h3>
      <el-button v-if="notifs.length > 0" @click="markAll" size="small">全部已读</el-button>
    </div>

    <el-card style="margin-top:20px;max-width:800px">
      <div v-if="notifs.length === 0" style="text-align:center;color:#999;padding:60px">
        <p style="font-size:48px">🔔</p>
        <p>暂无通知</p>
        <p style="font-size:13px">评价、信用分变动、组局消息会显示在这里</p>
      </div>

      <div v-for="n in notifs" :key="n.notificationId"
        :class="['notif-item', { unread: !n.isRead }]"
        @click="handleClick(n)">
        <div class="notif-header">
          <span class="notif-icon">{{ typeIcon(n.msgType) }}</span>
          <div class="notif-body">
            <div class="notif-title">
              <strong>{{ n.title }}</strong>
              <el-tag :type="typeTag(n.msgType)" size="small" class="type-tag">
                {{ typeLabel(n.msgType) }}
              </el-tag>
              <span v-if="!n.isRead" class="unread-dot"></span>
            </div>
            <p class="notif-content">{{ n.content }}</p>
            <span class="notif-time">{{ formatTime(n.createTime) }}</span>
          </div>
          <el-icon v-if="n.relatedId || n.msgType === 'EVALUATION'" class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { notificationAPI } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const notifs = ref([])

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function typeIcon(t) {
  const map = {
    APPLICATION: '📩', APPROVAL: '✅', REJECTION: '❌',
    EVALUATION: '⭐', CREDIT_CHANGE: '💰', ANNOUNCEMENT: '📢'
  }
  return map[t] || '📌'
}

function typeLabel(t) {
  const map = {
    APPLICATION: '申请', APPROVAL: '通过', REJECTION: '拒绝',
    EVALUATION: '评价', CREDIT_CHANGE: '积分', ANNOUNCEMENT: '公告'
  }
  return map[t] || '消息'
}

function typeTag(t) {
  const map = {
    APPLICATION: 'info', APPROVAL: 'success', REJECTION: 'danger',
    EVALUATION: 'warning', CREDIT_CHANGE: '', ANNOUNCEMENT: 'info'
  }
  return map[t] || 'info'
}

function handleClick(n) {
  if (!n.isRead) {
    notificationAPI.markRead(n.notificationId)
    n.isRead = true
    userStore.unreadCount = Math.max(0, userStore.unreadCount - 1)
  }

  // 根据消息类型跳转
  if (n.relatedId) {
    if (n.msgType === 'EVALUATION' || n.msgType === 'APPLICATION'
        || n.msgType === 'APPROVAL' || n.msgType === 'REJECTION') {
      router.push(`/group/${n.relatedId}`)
    }
  } else if (n.msgType === 'CREDIT_CHANGE') {
    router.push('/profile')
  } else if (n.msgType === 'ANNOUNCEMENT') {
    router.push('/')
  }
}

async function fetchNotifs() {
  try {
    const res = await notificationAPI.list()
    notifs.value = res.data || []
  } catch { /* ignore */ }
}

async function markAll() {
  await notificationAPI.markAllRead()
  notifs.value.forEach(n => n.isRead = true)
  userStore.unreadCount = 0
}

onMounted(fetchNotifs)
</script>

<style scoped>
.notif-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.notif-item:last-child { border-bottom: none; }
.notif-item.unread { background: #f0f9ff; }
.notif-item:hover { background: #f5f7fa; }

.notif-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.notif-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.type-tag {
  font-size: 11px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
  margin-left: auto;
}

.notif-content {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.notif-time {
  font-size: 12px;
  color: #bbb;
  margin-top: 4px;
  display: block;
}

.arrow-icon {
  color: #ccc;
  margin-top: 8px;
  flex-shrink: 0;
}
</style>
