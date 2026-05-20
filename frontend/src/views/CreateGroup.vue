<template>
  <div class="create-group">
    <el-page-header @back="$router.push('/')" title="返回" content="发起约饭" />
    <el-card style="margin-top:20px;max-width:600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：午饭搭子，第一食堂走起" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="食堂" prop="canteen">
          <el-select v-model="form.canteen" placeholder="选择食堂" style="width:100%">
            <el-option-group v-for="campus in campuses" :key="campus.name" :label="campus.name">
              <el-option v-for="ct in campus.canteens" :key="ct.name"
                :label="`${ct.name} — ${ct.desc}`" :value="ct.name" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="用餐时间" prop="mealTime">
          <el-date-picker v-model="form.mealTime" type="datetime" placeholder="选择日期和时间"
            style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="人数上限" prop="maxPeople">
          <el-input-number v-model="form.maxPeople" :min="2" :max="20" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit
            placeholder="说说想吃什么，对饭友有什么要求..." />
          <el-button size="small" style="margin-top:8px" @click="handlePolish" :loading="polishing">
            <el-icon><MagicStick /></el-icon> AI 智能润色
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">发起约饭</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { groupAPI, aiAPI } from '../api'
import { campuses, canteenNames, canteenMap } from '../data/canteens'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const polishing = ref(false)
const form = reactive({ title: '', canteen: '', mealTime: '', maxPeople: 4, remark: '' })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  canteen: [{ required: true, message: '请选择食堂', trigger: 'change' }],
  mealTime: [{ required: true, message: '请选择用餐时间', trigger: 'change' }],
  maxPeople: [{ required: true, message: '请设置人数', trigger: 'blur' }]
}

async function handlePolish() {
  if (!form.canteen) { ElMessage.warning('请先选择食堂'); return }
  polishing.value = true
  try {
    const res = await aiAPI.polish({
      canteen: form.canteen,
      mealTime: form.mealTime || '近期',
      remark: form.remark
    })
    const text = res.data?.reply || ''
    if (text) {
      form.remark = text.trim()
      ElMessage.success('AI润色完成')
    }
  } catch {
    ElMessage.warning('AI润色暂不可用，请手动填写')
  } finally { polishing.value = false }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await groupAPI.create(form)
    ElMessage.success('约饭组创建成功！')
    router.push('/')
  } catch { /* handled */ }
  finally { loading.value = false }
}
</script>
