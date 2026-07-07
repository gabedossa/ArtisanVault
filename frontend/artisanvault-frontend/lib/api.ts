import axios from 'axios'

export const API_ORIGIN = 'http://localhost:8080'

const api = axios.create({
  baseURL: `${API_ORIGIN}/api`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

export default api
