<template>
  <div class="ai-assistant">
    <h3>AI 美食助手 <el-tag v-if="offline" type="warning" size="small">离线模式</el-tag></h3>
    <el-card style="margin-top:20px;max-width:800px">
      <div class="chat-container">
        <div class="chat-messages" ref="chatBox">
          <div v-if="messages.length === 0" style="text-align:center;color:#999;padding:40px">
            <p style="font-size:48px">🤖</p>
            <p>你好！我是你的校园美食AI助手</p>
            <p style="margin-bottom:4px">我了解上理工所有食堂，可以帮你：</p>
            <div class="quick-actions">
              <el-tag v-for="q in quickQuestions" :key="q" class="quick-tag" @click="sendQuick(q)">{{ q }}</el-tag>
            </div>
          </div>
          <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">
            <div class="msg-avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
            <div class="msg-content" v-text="msg.content"></div>
            <el-tag v-if="msg.fallback" type="warning" size="small" class="fallback-tag">离线</el-tag>
          </div>
          <div v-if="waiting" class="message ai">
            <div class="msg-avatar">AI</div>
            <div class="msg-content thinking">思考中<span class="dots">...</span></div>
          </div>
        </div>
        <div class="chat-input">
          <el-input v-model="input" placeholder="问问食堂、美食、或者约饭建议..." @keyup.enter="send" :disabled="waiting">
            <template #append>
              <el-button @click="send" :loading="waiting">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiAPI } from '../api'

const messages = ref([])
const input = ref('')
const waiting = ref(false)
const offline = ref(false)
const chatBox = ref(null)

const quickQuestions = [
  '推荐食堂',
  '北校区有什么好吃的',
  '减脂餐推荐',
  '信用分怎么查'
]

function sendQuick(q) {
  input.value = q
  send()
}

async function send() {
  const q = input.value.trim()
  if (!q || waiting.value) return
  messages.value.push({ role: 'user', content: q })
  input.value = ''
  waiting.value = true
  await nextTick()
  scrollDown()

  try {
    const res = await aiAPI.chat(q)
    const reply = res.data.reply || ''
    const fallback = res.data.fallback || false
    messages.value.push({ role: 'ai', content: reply, fallback })
    if (fallback) offline.value = true
  } catch {
    messages.value.push({ role: 'ai', content: '抱歉，AI服务暂时不可用，请稍后再试。', fallback: true })
    offline.value = true
  } finally {
    waiting.value = false
    await nextTick()
    scrollDown()
  }
}

function scrollDown() {
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
}
</script>

<style scoped>
.chat-container { display: flex; flex-direction: column; height: 500px; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; background: #f9fafb; border-radius: 8px; }
.message { display: flex; gap: 12px; margin-bottom: 16px; align-items: flex-start; }
.message.user { flex-direction: row-reverse; }
.message.user .msg-content { background: #409eff; color: white; }
.message.ai .msg-content { background: white; border: 1px solid #e4e7ed; }
.msg-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: bold; flex-shrink: 0;
  background: #e4e7ed; color: #666;
}
.message.user .msg-avatar { background: #409eff; color: white; }
.msg-content {
  padding: 10px 14px; border-radius: 12px; max-width: 70%; white-space: pre-wrap; line-height: 1.6;
}
.msg-content.thinking { color: #999; font-style: italic; }
.fallback-tag { flex-shrink: 0; margin-top: 2px; font-size: 10px; }
.chat-input { margin-top: 12px; }
.quick-actions { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.quick-tag { cursor: pointer; }
.quick-tag:hover { opacity: 0.8; }
</style>
