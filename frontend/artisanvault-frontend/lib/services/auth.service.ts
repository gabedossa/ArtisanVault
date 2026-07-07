import api from '../api'
import { LoginRequest, LoginResponse } from '@/types'

export const authService = {
  async login(data: LoginRequest): Promise<LoginResponse> {
    const res = await api.post<LoginResponse>('/login', data)
    return res.data
  },

  async logout(): Promise<void> {
    await api.post('/login/logout')
  },

  async me(): Promise<LoginResponse> {
    const res = await api.get<LoginResponse>('/login/me')
    return res.data
  },
}
