import axios from 'axios'

export const API_ORIGIN = 'http://localhost:8080'

const api = axios.create({
  baseURL: `${API_ORIGIN}/api`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
  // Backend e frontend rodam em origens diferentes (portas distintas), então o
  // axios só envia o cookie XSRF-TOKEN de volta como header X-XSRF-TOKEN se isso
  // for habilitado explicitamente.
  withXSRFToken: true,
})

export default api
