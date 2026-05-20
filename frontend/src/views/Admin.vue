<template>
  <div class="admin">
    <h3>管理后台</h3>

    <el-row :gutter="16" style="margin-top:20px">
      <el-col :span="6" v-for="(card, i) in statsCards" :key="i">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px">
      <el-tabs v-model="adminTab">
        <el-tab-pane label="用户管理" name="users">
          <div style="margin-bottom:12px;display:flex;gap:8px">
            <el-input v-model="userKeyword" placeholder="搜索用户名或邮箱" style="width:260px" clearable
              @keyup.enter="fetchUsers" @clear="fetchUsers" />
            <el-button type="primary" @click="fetchUsers">搜索</el-button>
          </div>
          <div v-if="userError" style="color:#f56c6c;padding:12px">加载失败: {{ userError }}</div>
          <el-table :data="users" v-loading="loadingUsers" v-else>
            <el-table-column prop="userId" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="creditScore" label="信用分" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '封禁' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{row}">
                <el-button v-if="row.status === 1" type="danger" size="small" @click="handleBan(row.userId)">封禁</el-button>
                <el-button v-else type="success" size="small" @click="handleUnban(row.userId)">解封</el-button>
              </template>
            </el-table-column>
            <template #empty>暂无用户数据</template>
          </el-table>
          <div style="margin-top:12px;text-align:center" v-if="!userError">
            <el-pagination v-model:current-page="userPage" :page-size="10"
              :total="userTotal" layout="prev, pager, next" @current-change="fetchUsers" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="约饭组管理" name="groups">
          <div v-if="groupError" style="color:#f56c6c;padding:12px">加载失败: {{ groupError }}</div>
          <el-table :data="adminGroups" v-loading="loadingGroups" v-else>
            <el-table-column prop="groupId" label="ID" width="80" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="canteen" label="食堂" width="120" />
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{row}">
                <el-button v-if="row.status !== 'CANCELLED'" type="danger" size="small" @click="handleDeleteGroup(row.groupId)">取消</el-button>
              </template>
            </el-table-column>
            <template #empty>暂无约饭组数据</template>
          </el-table>
          <div style="margin-top:12px;text-align:center" v-if="!groupError">
            <el-pagination v-model:current-page="groupPage" :page-size="10"
              :total="groupTotal" layout="prev, pager, next" @current-change="fetchGroups" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const adminTab = ref('users')
const stats = ref({ totalUsers: 0, totalGroups: 0, activeGroups: 0, activeRate: '0%' })
const users = ref([])
const adminGroups = ref([])
const loadingUsers = ref(false), loadingGroups = ref(false)
const userError = ref(''), groupError = ref('')
const userPage = ref(1), userTotal = ref(0)
const groupPage = ref(1), groupTotal = ref(0)
const userKeyword = ref('')

const statsCards = computed(() => [
  { label: '总用户数', value: stats.value.totalUsers },
  { label: '总约饭组', value: stats.value.totalGroups },
  { label: '活跃约饭组', value: stats.value.activeGroups },
  { label: '活跃率', value: stats.value.activeRate }
])

function statusText(s) {
  const map = { RECRUITING: '招募中', CONFIRMED: '已组队', FINISHED: '已完成', CANCELLED: '已取消' }
  return map[s] || s
}

function statusType(s) {
  const map = { RECRUITING: 'success', CONFIRMED: 'warning', FINISHED: 'info', CANCELLED: 'danger' }
  return map[s] || 'info'
}

async function fetchStats() {
  try {
    const res = await adminAPI.stats()
    stats.value = res.data
  } catch (e) {
    /* stats API might fail, ignore */
  }
}

async function fetchUsers() {
  loadingUsers.value = true
  userError.value = ''
  try {
    const params = { page: userPage.value - 1, size: 10 }
    if (userKeyword.value) params.keyword = userKeyword.value
    const res = await adminAPI.users(params)
    users.value = res.data.content || []
    userTotal.value = res.data.totalElements || 0
    console.log('用户数据:', res.data)
  } catch (e) {
    userError.value = e.message || '请求失败'
    console.error('获取用户失败:', e)
  } finally {
    loadingUsers.value = false
  }
}

async function fetchGroups() {
  loadingGroups.value = true
  groupError.value = ''
  try {
    const res = await adminAPI.groups({ page: groupPage.value - 1, size: 10 })
    adminGroups.value = res.data.content || []
    groupTotal.value = res.data.totalElements || 0
  } catch (e) {
    groupError.value = e.message || '请求失败'
    console.error('获取约饭组失败:', e)
  } finally {
    loadingGroups.value = false
  }
}

async function handleBan(id) {
  try {
    await ElMessageBox.confirm('确定封禁该用户？', '确认', { type: 'warning' })
    await adminAPI.banUser(id)
    ElMessage.success('已封禁')
    fetchUsers()
  } catch { /* cancel */ }
}

async function handleUnban(id) {
  await adminAPI.unbanUser(id)
  ElMessage.success('已解封')
  fetchUsers()
}

async function handleDeleteGroup(id) {
  try {
    await ElMessageBox.confirm('确定取消该约饭组？', '确认', { type: 'warning' })
    await adminAPI.deleteGroup(id)
    ElMessage.success('已取消')
    fetchGroups()
  } catch { /* cancel */ }
}

onMounted(() => {
  fetchStats()
  fetchUsers()
  fetchGroups()
})
</script>

<style scoped>
.stat-card { text-align: center; padding: 16px 0; }
.stat-value { font-size: 32px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 14px; color: #999; margin-top: 8px; }
</style>
