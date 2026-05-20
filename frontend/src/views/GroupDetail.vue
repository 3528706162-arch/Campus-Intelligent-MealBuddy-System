<template>
  <div class="group-detail" v-loading="loading">
    <el-page-header @back="$router.push('/')" title="返回">
      <template #content>
        <span>{{ group.title }}</span>
      </template>
    </el-page-header>

    <el-card style="margin-top:20px" v-if="group.groupId">
      <h3>{{ group.title }}</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="食堂">{{ group.canteen }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType">{{ statusText(group.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用餐时间">{{ formatTime(group.mealTime) }}</el-descriptions-item>
        <el-descriptions-item label="人数">{{ group.currentPeople }}/{{ group.maxPeople }}人</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ group.initiatorName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(group.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <p style="margin-top:12px;color:#666" v-if="group.remark">备注：{{ group.remark }}</p>

      <div style="margin-top:20px;display:flex;gap:12px">
        <el-button type="primary"
          v-if="group.status === 'RECRUITING' && group.initiatorId !== userStore.user?.userId && userStore.isLoggedIn"
          @click="handleApply">申请加入</el-button>
        <el-button type="warning"
          v-if="group.status === 'CONFIRMED' && group.initiatorId === userStore.user?.userId"
          @click="handleComplete">完成约饭</el-button>
        <el-button type="danger"
          v-if="(group.status === 'RECRUITING' || group.status === 'CONFIRMED') && group.initiatorId === userStore.user?.userId"
          @click="handleCancel">取消约饭</el-button>
      </div>
    </el-card>

    <!-- 申请列表（仅发起人可见）-->
    <el-card style="margin-top:20px" v-if="group.initiatorId === userStore.user?.userId && applications.length > 0">
      <h4>加入申请 ({{ applications.length }})</h4>
      <el-table :data="applications" style="margin-top:12px">
        <el-table-column label="申请人">
          <template #default="{row}">{{ row.user?.username }}</template>
        </el-table-column>
        <el-table-column label="申请时间">
          <template #default="{row}">{{ formatTime(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{row}">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : 'danger'" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" v-if="group.status === 'RECRUITING'">
          <template #default="{row}">
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleApprove(row.applicationId)">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="handleReject(row.applicationId)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 评价区域 -->
    <el-card style="margin-top:20px" v-if="group.groupId">
      <h4>饭友评价 ({{ evaluations.length }})</h4>
      <div v-if="group.status === 'FINISHED' && userStore.isLoggedIn">
        <el-form :model="evalForm" style="margin-top:12px">
          <el-form-item label="评价用户">
            <el-select v-model="evalForm.toUserId" placeholder="选择要评价的用户">
              <el-option v-for="p in participants" :key="p.userId"
                :label="p.username" :value="p.userId"
                :disabled="p.userId === userStore.user?.userId" />
            </el-select>
          </el-form-item>
          <el-form-item label="评分">
            <el-rate v-model="evalForm.rating" />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input v-model="evalForm.content" type="textarea" :rows="2" maxlength="300" show-word-limit
              placeholder="说说这次约饭的感受..." />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="evalForm.anonymous">匿名评价</el-checkbox>
          </el-form-item>
          <el-button type="primary" @click="submitEval">提交评价</el-button>
        </el-form>
      </div>
      <div v-if="evaluations.length === 0 && (!userStore.isLoggedIn || group.status !== 'FINISHED')" style="text-align:center;padding:20px;color:#999">
        暂无评价
      </div>
      <div v-for="e in evaluations" :key="e.evaluationId" class="eval-item" style="margin-top:12px">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <el-rate :model-value="Number(e.rating)" disabled size="small" />
            <span style="margin-left:8px;font-size:13px;color:#999">
              {{ e.anonymous ? '匿名用户' : (e.fromUsername || '用户') }} → {{ e.toUsername || '用户' }}
            </span>
          </div>
          <span style="font-size:12px;color:#999">{{ formatTime(e.createTime) }}</span>
        </div>
        <p style="margin-top:4px;font-size:14px;color:#666" v-if="e.content">{{ e.content }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { groupAPI, applicationAPI, evaluationAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const group = ref({})
const applications = ref([])
const evaluations = ref([])
const participants = ref([])
const evalForm = reactive({ toUserId: null, rating: 3, content: '', anonymous: false })

const statusType = computed(() => {
  const map = { RECRUITING: 'success', CONFIRMED: 'warning', FINISHED: 'info', CANCELLED: 'danger' }
  return map[group.value.status] || 'info'
})

function statusText(s) {
  const map = { RECRUITING: '招募中', CONFIRMED: '已组队', FINISHED: '已完成', CANCELLED: '已取消', PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[s] || s
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function fetchDetail() {
  loading.value = true
  try {
    const res = await groupAPI.detail(route.params.id)
    group.value = res.data
    if (group.value.initiatorId === userStore.user?.userId) {
      await fetchApplications()
    }
    await fetchParticipants()
    await fetchEvaluations()
  } finally { loading.value = false }
}

async function fetchApplications() {
  try {
    const res = await applicationAPI.getGroupApps(route.params.id)
    applications.value = res.data
  } catch { /* ignore */ }
}

async function handleApply() {
  await applicationAPI.apply(route.params.id, {})
  ElMessage.success('申请已提交')
}

async function handleApprove(id) {
  await applicationAPI.approve(id)
  ElMessage.success('已通过')
  fetchDetail()
}

async function handleReject(id) {
  await applicationAPI.reject(id)
  ElMessage.success('已拒绝')
  fetchDetail()
}

async function handleCancel() {
  await ElMessageBox.confirm('确定要取消这个约饭组吗？临近用餐时间取消会扣信用分。', '确认取消', { type: 'warning' })
  await groupAPI.cancel(route.params.id)
  ElMessage.success('已取消')
  fetchDetail()
}

async function handleComplete() {
  await ElMessageBox.confirm('确认完成约饭？完成后即可互相评价。', '确认完成', { type: 'info' })
  await groupAPI.complete(route.params.id)
  ElMessage.success('已完成')
  fetchDetail()
}

async function fetchParticipants() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await groupAPI.participants(route.params.id)
    participants.value = res.data || []
  } catch { /* ignore */ }
}

async function fetchEvaluations() {
  try {
    const res = await evaluationAPI.getGroupEvals(route.params.id)
    evaluations.value = Array.isArray(res.data) ? res.data : []
  } catch { evaluations.value = [] }
}

async function submitEval() {
  if (!evalForm.toUserId) { ElMessage.warning('请选择要评价的用户'); return }
  await evaluationAPI.submit(route.params.id, {
    toUserId: evalForm.toUserId,
    rating: evalForm.rating,
    content: evalForm.content,
    anonymous: evalForm.anonymous
  })
  ElMessage.success('评价提交成功')
  evalForm.toUserId = null
  evalForm.content = ''
  evalForm.rating = 3
  evalForm.anonymous = false
}

onMounted(fetchDetail)
</script>
