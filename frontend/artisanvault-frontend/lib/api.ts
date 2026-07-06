import axios from 'axios'

export const API_ORIGIN = 'http://localhost:8080'

const api = axios.create({
  baseURL: `${API_ORIGIN}/api`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('artisanvault_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

export default api
