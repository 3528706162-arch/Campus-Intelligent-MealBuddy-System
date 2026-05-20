<template>
  <div class="my-groups">
    <h3>我的约饭</h3>

    <el-tabs v-model="tab" style="margin-top:16px">
      <el-tab-pane label="我发起的" name="created" />
      <el-tab-pane label="我申请的" name="applied" />
    </el-tabs>

    <div v-if="tab === 'created'">
      <div v-if="myGroups.length === 0" style="text-align:center;color:#999;padding:40px">暂无发起的约饭</div>
      <div class="group-list">
        <el-card v-for="g in myGroups" :key="g.groupId" shadow="hover" style="margin-bottom:12px;cursor:pointer"
          @click="$router.push(`/group/${g.groupId}`)">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <div>
              <el-tag :type="statusType(g.status)" size="small" style="margin-right:8px">{{ statusText(g.status) }}</el-tag>
              <strong>{{ g.title }}</strong>
            </div>
            <span style="color:#999;font-size:13px">{{ g.canteen }} | {{ g.currentPeople }}/{{ g.maxPeople }}人</span>
          </div>
        </el-card>
      </div>
    </div>

    <div v-if="tab === 'applied'">
      <div v-if="myApps.length === 0" style="text-align:center;color:#999;padding:40px">暂无申请记录</div>
      <el-card v-for="app in myApps" :key="app.applicationId" shadow="hover" style="margin-bottom:12px">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <el-tag :type="statusType(app.status)" size="small" style="margin-right:8px">{{ statusText(app.status) }}</el-tag>
            <strong>{{ app.group?.title }}</strong>
            <span style="color:#999;font-size:13px;margin-left:8px">{{ app.group?.canteen }}</span>
          </div>
          <div>
            <span style="color:#999;font-size:12px;margin-right:12px">{{ formatTime(app.applyTime) }}</span>
            <el-button v-if="app.status === 'PENDING'" type="danger" size="small" @click="cancelApp(app.applicationId)">取消</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { groupAPI, applicationAPI } from '../api'
import { ElMessage } from 'element-plus'

const tab = ref('created')
const myGroups = ref([])
const myApps = ref([])

function statusText(s) {
  const map = { RECRUITING: '招募中', CONFIRMED: '已组队', FINISHED: '已完成', CANCELLED: '已取消', PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[s] || s
}

function statusType(s) {
  const map = { RECRUITING: 'success', CONFIRMED: 'warning', FINISHED: 'info', CANCELLED: 'danger', PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[s] || 'info'
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

async function cancelApp(id) {
  await applicationAPI.cancel(id)
  ElMessage.success('已取消')
  fetchApps()
}

async function fetchGroups() {
  try {
    const res = await groupAPI.myGroups()
    myGroups.value = res.data
  } catch { /* ignore */ }
}

async function fetchApps() {
  try {
    const res = await applicationAPI.myApps()
    myApps.value = res.data
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchGroups()
  fetchApps()
})
</script>
