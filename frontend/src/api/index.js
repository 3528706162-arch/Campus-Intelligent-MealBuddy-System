import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code != 200 && data.code !== undefined) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  error => {
    ElMessage.error(error.response?.data?.message || '网络错误')
    return Promise.reject(error)
  }
)

export default api

// Auth
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data)
}

// User
export const userAPI = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data) => api.put('/user/profile', data),
  uploadAvatar: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  getPreference: () => api.get('/user/preference'),
  updatePreference: (data) => api.put('/user/preference', data)
}

// Group
export const groupAPI = {
  create: (data) => api.post('/group', data),
  list: (params) => api.get('/group', { params }),
  detail: (id) => api.get(`/group/${id}`),
  cancel: (id) => api.delete(`/group/${id}`),
  complete: (id) => api.put(`/group/${id}/complete`),
  myGroups: () => api.get('/group/my'),
  participants: (id) => api.get(`/group/${id}/participants`)
}

// Application
export const applicationAPI = {
  apply: (groupId, data) => api.post(`/application/${groupId}`, data),
  approve: (id) => api.put(`/application/${id}/approve`),
  reject: (id) => api.put(`/application/${id}/reject`),
  cancel: (id) => api.delete(`/application/${id}`),
  getGroupApps: (groupId) => api.get(`/application/group/${groupId}`),
  myApps: () => api.get('/application/my')
}

// Evaluation
export const evaluationAPI = {
  submit: (groupId, data) => api.post(`/evaluation/${groupId}`, data),
  getUserEvals: (userId) => api.get(`/evaluation/user/${userId}`),
  getUserRating: (userId) => api.get(`/evaluation/user/${userId}/rating`),
  getGroupEvals: (groupId) => api.get(`/evaluation/group/${groupId}`)
}

// Notification
export const notificationAPI = {
  list: () => api.get('/notification'),
  unread: () => api.get('/notification/unread'),
  unreadCount: () => api.get('/notification/unread/count'),
  markRead: (id) => api.put(`/notification/${id}/read`),
  markAllRead: () => api.put('/notification/read-all')
}

// AI Chat
export const aiAPI = {
  chat: (question) => api.post('/ai/chat', { question }),
  polish: (data) => api.post('/ai/polish', data),
  history: () => api.get('/ai/history')
}

// Admin
export const adminAPI = {
  stats: () => api.get('/admin/stats'),
  users: (params) => api.get('/admin/users', { params }),
  banUser: (id) => api.put(`/admin/users/${id}/ban`),
  unbanUser: (id) => api.put(`/admin/users/${id}/unban`),
  deleteGroup: (id) => api.delete(`/admin/groups/${id}`),
  groups: (params) => api.get('/admin/groups', { params }),
  createAnnouncement: (data) => api.post('/admin/announcement', data)
}
