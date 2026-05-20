import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI, userAPI, groupAPI } from '../api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const unreadCount = ref(0)
  const myGroupCount = ref(0)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isInitiator = computed(() => myGroupCount.value > 0)

  const roleText = computed(() => {
    if (user.value?.role === 'ADMIN') return '管理员'
    if (myGroupCount.value > 0) return '组局发起人'
    return '普通用户'
  })

  const roleTagType = computed(() => {
    if (user.value?.role === 'ADMIN') return 'danger'
    if (myGroupCount.value > 0) return 'warning'
    return 'info'
  })

  async function login(email, password) {
    const res = await authAPI.login({ email, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchProfile()
    await fetchMyGroupCount()
    return res
  }

  async function register(data) {
    const res = await authAPI.register(data)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchProfile()
    return res
  }

  async function fetchProfile() {
    try {
      const res = await userAPI.getProfile()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch { /* ignore */ }
  }

  async function fetchMyGroupCount() {
    try {
      const res = await groupAPI.myGroups()
      myGroupCount.value = Array.isArray(res.data) ? res.data.length : 0
    } catch { myGroupCount.value = 0 }
  }

  function logout() {
    token.value = ''
    user.value = null
    myGroupCount.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, unreadCount, myGroupCount, isLoggedIn, isAdmin, isInitiator, roleText, roleTagType, login, register, fetchProfile, fetchMyGroupCount, logout }
})
