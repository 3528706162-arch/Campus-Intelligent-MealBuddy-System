<template>
  <div class="profile">
    <el-row :gutter="16">
      <!-- 左侧：用户信息卡片 -->
      <el-col :span="8">
        <el-card>
          <div style="text-align:center;padding:20px 0">
            <!-- 头像上传 -->
            <div class="avatar-section" @click="triggerUpload">
              <el-avatar :size="88" :src="avatarUrl">
                {{ userStore.user?.username?.charAt(0) }}
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon><Camera /></el-icon>
                <span>换头像</span>
              </div>
            </div>
            <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />

            <h3 style="margin:12px 0 4px">{{ userStore.user?.username }}</h3>
            <el-tag :type="userStore.roleTagType" size="small">{{ userStore.roleText }}</el-tag>
            <p style="color:#999;font-size:13px;margin-top:4px">{{ userStore.user?.email }}</p>

            <div class="credit-display">
              <el-progress
                type="dashboard"
                :percentage="creditPercent"
                :color="creditColor"
                :width="140"
              >
                <template #default>
                  <span class="credit-num">{{ userStore.user?.creditScore }}</span>
                  <span class="credit-sub">信用分</span>
                </template>
              </el-progress>
            </div>
            <el-tag :type="creditLevel.type" size="large" effect="dark">{{ creditLevel.text }}</el-tag>
          </div>
          <el-divider />
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="手机">{{ userStore.user?.phone || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatTime(userStore.user?.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="平均评分">
              <el-rate :model-value="avgRating" disabled show-score text-color="#ff9900" size="small" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右侧 -->
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <!-- 编辑资料 -->
          <el-tab-pane label="编辑资料" name="edit">
            <el-card>
              <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="80px" style="max-width:440px">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" placeholder="输入新用户名" maxlength="20" show-word-limit />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input :model-value="userStore.user?.email" disabled>
                    <template #append>不可修改</template>
                  </el-input>
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="输入手机号" maxlength="11" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile" :loading="savingProfile">保存资料</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <!-- 口味偏好 -->
          <el-tab-pane label="口味偏好" name="pref">
            <el-card>
              <el-form :model="prefForm" label-width="100px" style="max-width:500px">
                <el-form-item label="口味偏好">
                  <el-select v-model="prefForm.tasteTags" multiple placeholder="选择口味" style="width:100%">
                    <el-option label="本帮菜" value="本帮菜" />
                    <el-option label="川湘菜" value="川湘菜" />
                    <el-option label="粤菜" value="粤菜" />
                    <el-option label="西北菜" value="西北菜" />
                    <el-option label="面食" value="面食" />
                    <el-option label="麻辣烫/香锅" value="麻辣烫" />
                    <el-option label="小吃" value="小吃" />
                    <el-option label="西餐" value="西餐" />
                    <el-option label="清真" value="清真" />
                    <el-option label="减脂轻食" value="减脂轻食" />
                    <el-option label="烧烤" value="烧烤" />
                    <el-option label="烘焙甜点" value="烘焙甜点" />
                    <el-option label="盖浇饭" value="盖浇饭" />
                  </el-select>
                </el-form-item>
                <el-form-item label="忌口">
                  <el-input v-model="prefForm.taboo" placeholder="例如：不吃香菜、不吃辣、不吃葱" />
                </el-form-item>
                <el-form-item label="常去食堂">
                  <el-select v-model="prefForm.favoriteCanteen" placeholder="选择食堂" style="width:100%">
                    <el-option-group v-for="campus in campuses" :key="campus.name" :label="campus.name">
                      <el-option v-for="ct in campus.canteens" :key="ct.name"
                        :label="ct.name" :value="ct.name" />
                    </el-option-group>
                  </el-select>
                </el-form-item>
                <el-form-item label="用餐时段">
                  <el-select v-model="prefForm.mealTimePeriod" multiple placeholder="选择时段" style="width:100%">
                    <el-option label="11:30-12:30" value="11:30-12:30" />
                    <el-option label="12:00-13:00" value="12:00-13:00" />
                    <el-option label="17:30-18:30" value="17:30-18:30" />
                    <el-option label="18:00-19:00" value="18:00-19:00" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="savePref" :loading="saving">保存偏好</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <!-- 评价记录 -->
          <el-tab-pane label="评价记录" name="evals">
            <el-card>
              <div v-if="evaluations.length === 0" style="text-align:center;padding:40px;color:#999">
                <p style="font-size:36px">⭐</p>
                <p>暂无评价记录</p>
                <p style="font-size:13px">完成约饭后即可互相评价</p>
              </div>
              <div v-for="e in evaluations" :key="e.evaluationId" class="eval-item">
                <div class="eval-header">
                  <div style="display:flex;align-items:center;gap:8px">
                    <el-rate :model-value="Number(e.rating)" disabled size="small" />
                    <span v-if="e.anonymous" style="color:#999;font-size:12px">匿名用户</span>
                    <span v-else style="font-size:12px;color:#666">{{ e.fromUsername }}</span>
                  </div>
                  <span class="eval-time">{{ formatTime(e.createTime) }}</span>
                </div>
                <p class="eval-content" v-if="e.content">{{ e.content }}</p>
                <p class="eval-content" v-else style="color:#ccc">未填写评价内容</p>
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '../stores/user'
import { userAPI, evaluationAPI } from '../api'
import { campuses, canteenNames } from '../data/canteens'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('edit')
const saving = ref(false)
const savingProfile = ref(false)
const evaluations = ref([])
const avgRating = ref(0)
const fileInput = ref(null)
const avatarUrl = computed(() => userStore.user?.avatarUrl || '')

const profileForm = reactive({ username: '', phone: '' })
const profileFormRef = ref(null)
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名2-20字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^(1[3-9]\d{9})?$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const prefForm = reactive({ tasteTags: [], taboo: '', favoriteCanteen: '', mealTimePeriod: [] })

const creditPercent = computed(() => {
  return Math.min(100, parseFloat(userStore.user?.creditScore || 0))
})

const creditColor = computed(() => {
  const score = parseFloat(userStore.user?.creditScore || 0)
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
})

const creditLevel = computed(() => {
  const score = parseFloat(userStore.user?.creditScore || 0)
  if (score >= 90) return { type: 'success', text: '信用优秀' }
  if (score >= 70) return { type: 'info', text: '信用良好' }
  if (score >= 60) return { type: 'warning', text: '信用一般' }
  return { type: 'danger', text: '信用受限' }
})

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

// --- 头像上传 ---
function triggerUpload() {
  fileInput.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像大小不能超过5MB')
    return
  }
  try {
    const res = await userAPI.uploadAvatar(file)
    ElMessage.success('头像已更新')
    await userStore.fetchProfile()
  } catch { ElMessage.error('上传失败') }
  // 重置input
  if (fileInput.value) fileInput.value.value = ''
}

// --- 编辑资料 ---
async function saveProfile() {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingProfile.value = true
  try {
    await userAPI.updateProfile({
      username: profileForm.username,
      phone: profileForm.phone
    })
    ElMessage.success('资料已更新')
    await userStore.fetchProfile()
  } catch { /* handled */ }
  finally { savingProfile.value = false }
}

// --- 口味偏好 ---
async function savePref() {
  saving.value = true
  try {
    await userAPI.updatePreference({
      tasteTags: JSON.stringify(prefForm.tasteTags),
      taboo: prefForm.taboo,
      favoriteCanteen: prefForm.favoriteCanteen,
      mealTimePeriod: JSON.stringify(prefForm.mealTimePeriod)
    })
    ElMessage.success('偏好已更新')
  } finally { saving.value = false }
}

async function fetchEvaluations() {
  if (!userStore.user) return
  try {
    const [evalRes, ratingRes] = await Promise.all([
      evaluationAPI.getUserEvals(userStore.user.userId),
      evaluationAPI.getUserRating(userStore.user.userId)
    ])
    evaluations.value = evalRes.data || []
    avgRating.value = Math.round((ratingRes.data || 0) * 10) / 10
  } catch { /* ignore */ }
}

onMounted(async () => {
  // 刷新用户数据（信用分等）
  await userStore.fetchProfile()
  userStore.fetchMyGroupCount()

  // 填充编辑表单
  profileForm.username = userStore.user?.username || ''
  profileForm.phone = userStore.user?.phone || ''

  // 加载偏好
  try {
    const res = await userAPI.getPreference()
    if (res.data) {
      if (res.data.tasteTags) prefForm.tasteTags = JSON.parse(res.data.tasteTags)
      prefForm.taboo = res.data.taboo || ''
      prefForm.favoriteCanteen = res.data.favoriteCanteen || ''
      if (res.data.mealTimePeriod) prefForm.mealTimePeriod = JSON.parse(res.data.mealTimePeriod)
    }
  } catch { /* ignore */ }
  fetchEvaluations()
})
</script>

<style scoped>
.avatar-section {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0,0,0,0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
  font-size: 12px;
}

.avatar-section:hover .avatar-overlay {
  opacity: 1;
}

.credit-display { margin: 16px 0; display: flex; justify-content: center; }
.credit-num { font-size: 32px; font-weight: bold; }
.credit-sub { font-size: 12px; color: #999; display: block; }

.eval-item { padding: 16px; border-bottom: 1px solid #f5f5f5; }
.eval-item:last-child { border-bottom: none; }
.eval-header { display: flex; justify-content: space-between; align-items: center; }
.eval-time { font-size: 12px; color: #999; }
.eval-content { margin-top: 8px; font-size: 14px; color: #666; }
</style>
