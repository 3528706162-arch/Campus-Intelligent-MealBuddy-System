<template>
  <div class="home">
    <!-- 系统公告 -->
    <el-alert v-if="announcement" :title="announcement.title" :description="announcement.content"
      type="info" show-icon :closable="false" style="margin-bottom:16px">
      <template #default>
        <span style="font-size:13px;color:#666">{{ announcement.content }}</span>
      </template>
    </el-alert>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#e6f7ff"><el-icon :size="28" color="#1890ff"><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeGroups || 0 }}</div>
            <div class="stat-label">正在招募</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#f6ffed"><el-icon :size="28" color="#52c41a"><Checked /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalGroups || 0 }}</div>
            <div class="stat-label">累计约饭</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#fff7e6"><el-icon :size="28" color="#fa8c16"><Star /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">注册用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#f9f0ff"><el-icon :size="28" color="#722ed1"><TrendCharts /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.popularCanteen || '--' }}</div>
            <div class="stat-label">热门食堂</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 左侧主区域 -->
      <el-col :span="17">
        <el-card>
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span><el-icon><DishDot /></el-icon> <strong>约饭广场</strong></span>
              <el-input v-model="canteenFilter" placeholder="搜索食堂..." style="width:200px" clearable @change="fetchGroups" />
            </div>
          </template>

          <div v-if="groups.length === 0" style="text-align:center;padding:60px;color:#999">
            <el-icon :size="60" color="#ddd"><DishDot /></el-icon>
            <p style="margin-top:12px">暂无招募中的约饭组，快去发起第一个吧！</p>
          </div>

          <div v-for="g in groups" :key="g.groupId" class="group-item" @click="$router.push(`/group/${g.groupId}`)">
            <div class="group-left">
              <div class="group-avatar">
                {{ g.initiatorName?.charAt(0) }}
              </div>
              <div class="group-body">
                <div class="group-title">
                  <el-tag :type="statusType(g.status)" size="small" style="margin-right:6px">{{ statusText(g.status) }}</el-tag>
                  {{ g.title }}
                </div>
                <div class="group-meta">
                  <span><el-icon><Location /></el-icon> {{ g.canteen }}</span>
                  <span><el-icon><Clock /></el-icon> {{ formatTime(g.mealTime) }}</span>
                  <span><el-icon><User /></el-icon> {{ g.initiatorName }}</span>
                </div>
              </div>
            </div>
            <div class="group-right">
              <div class="people-badge">{{ g.currentPeople }}/{{ g.maxPeople }}</div>
              <span style="font-size:12px;color:#999">人</span>
            </div>
          </div>

          <div class="pagination" v-if="total > 10">
            <el-pagination
              v-model:current-page="page"
              :page-size="10"
              :total="total"
              layout="prev, pager, next"
              small
              @current-change="fetchGroups"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧栏 -->
      <el-col :span="7">
        <!-- 信用规则 -->
        <el-card style="margin-bottom:16px">
          <template #header>
            <span><el-icon><InfoFilled /></el-icon> <strong>信用规则</strong></span>
          </template>
          <div class="rule-list">
            <div class="rule-item"><span class="dot good"></span> 初始信用分：100分</div>
            <div class="rule-item"><span class="dot good"></span> 获得好评（4星以上）：+2分</div>
            <div class="rule-item"><span class="dot bad"></span> 获得差评（2星以下）：-3分</div>
            <div class="rule-item"><span class="dot bad"></span> 临近用餐取消：-20分</div>
            <div class="rule-item"><span class="dot warn"></span> 信用分低于60分将受限</div>
          </div>
        </el-card>

        <!-- 食堂导航 -->
        <el-card style="margin-bottom:16px">
          <template #header>
            <span><el-icon><Guide /></el-icon> <strong>食堂导航</strong></span>
          </template>
          <div v-for="campus in campuses.slice(0, 3)" :key="campus.name" class="campus-group">
            <div class="campus-label">{{ campus.name }}</div>
            <div class="canteen-list">
              <el-tag v-for="ct in campus.canteens" :key="ct.name"
                class="canteen-tag" @click="canteenFilter = ct.name; fetchGroups()">
                {{ ct.name }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <!-- 快速入口 -->
        <el-card v-if="!userStore.isLoggedIn">
          <template #header>
            <span><el-icon><Pointer /></el-icon> <strong>快速入口</strong></span>
          </template>
          <el-button type="primary" style="width:100%;margin-bottom:8px" @click="$router.push('/login')">登录</el-button>
          <el-button style="width:100%" @click="$router.push('/register')">注册新账号</el-button>
          <p style="margin-top:8px;font-size:12px;color:#999;text-align:center">登录后可体验完整功能</p>
        </el-card>
        <el-card v-else>
          <template #header>
            <span><el-icon><Pointer /></el-icon> <strong>快捷操作</strong></span>
          </template>
          <el-button type="primary" style="width:100%;margin-bottom:8px" @click="$router.push('/create-group')">
            <el-icon><Plus /></el-icon> 发起约饭
          </el-button>
          <el-button style="width:100%;margin-bottom:8px" @click="$router.push('/ai-assistant')">
            <el-icon><ChatDotRound /></el-icon> AI 美食推荐
          </el-button>
          <el-button style="width:100%" @click="$router.push('/my-groups')">
            <el-icon><List /></el-icon> 我的约饭记录
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { groupAPI, adminAPI, notificationAPI } from '../api'
import { campuses, canteenNames } from '../data/canteens'

const userStore = useUserStore()
const groups = ref([])
const page = ref(1)
const total = ref(0)
const canteenFilter = ref('')
const canteens = canteenNames
const announcement = ref(null)

const stats = reactive({
  activeGroups: 0,
  totalGroups: 0,
  totalUsers: 0,
  popularCanteen: ''
})

function statusText(s) {
  const map = { RECRUITING: '招募中', CONFIRMED: '已组队', FINISHED: '已完成', CANCELLED: '已取消' }
  return map[s] || s
}

function statusType(s) {
  const map = { RECRUITING: 'success', CONFIRMED: '', FINISHED: 'info', CANCELLED: 'danger' }
  return map[s] || 'info'
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function fetchGroups() {
  const params = { page: page.value - 1, size: 10 }
  if (canteenFilter.value) params.canteen = canteenFilter.value
  try {
    const res = await groupAPI.list(params)
    groups.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch { /* ignore */ }
}

async function fetchStats() {
  try {
    const res = await adminAPI.stats()
    stats.totalUsers = res.data.totalUsers || 0
    stats.totalGroups = res.data.totalGroups || 0
    stats.activeGroups = res.data.activeGroups || 0
    stats.popularCanteen = res.data.activeGroups > 0 ? '风味餐厅' : '--'
  } catch { /* ignore */ }
}

async function fetchAnnouncement() {
  try {
    const res = await notificationAPI.list()
    const list = res.data || []
    announcement.value = list.find(n => n.msgType === 'ANNOUNCEMENT') || null
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchGroups()
  fetchStats()
  fetchAnnouncement()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: default;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}
.group-item:hover { background: #f9fafb; }
.group-item:last-child { border-bottom: none; }
.group-left { display: flex; gap: 14px; align-items: center; flex: 1; }
.group-avatar {
  width: 44px; height: 44px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  flex-shrink: 0;
}
.group-title { font-size: 15px; font-weight: 500; margin-bottom: 6px; }
.group-meta { display: flex; gap: 16px; font-size: 12px; color: #999; }
.group-meta span { display: flex; align-items: center; gap: 2px; }
.group-right { text-align: center; }
.people-badge { font-size: 24px; font-weight: bold; color: #409eff; }
.pagination { margin-top: 16px; display: flex; justify-content: center; }

.rule-list { font-size: 13px; }
.rule-item { margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot.good { background: #52c41a; }
.dot.bad { background: #f5222d; }
.dot.warn { background: #fa8c16; }

.campus-group { margin-bottom: 12px; }
.campus-group:last-child { margin-bottom: 0; }
.campus-label { font-size: 12px; color: #999; margin-bottom: 6px; }
.canteen-list { display: flex; flex-wrap: wrap; gap: 6px; }
.canteen-tag { cursor: pointer; font-size: 12px; }
</style>
