'use client'

import { createContext, useContext, useEffect, useState, ReactNode } from 'react'
import { AuthUser, LoginRequest } from '@/types'
import { authService } from '@/lib/services/auth.service'

interface AuthContextType {
  user: AuthUser | null
  loading: boolean
  login: (data: LoginRequest) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const stored = localStorage.getItem('artisanvault_user')
    const token = localStorage.getItem('artisanvault_token')
    if (stored && token) {
      setUser(JSON.parse(stored))
    } else if (stored) {
      localStorage.removeItem('artisanvault_user')
      localStorage.removeItem('artisanvault_token')
    }
    setLoading(false)
  }, [])

  const login = async (data: LoginRequest) => {
    const res = await authService.login(data)

    const authUser: AuthUser = {
      email: res.email,
      userType: res.userType,
      userId: res.userId,
      userName: res.nome,
    }
    setUser(authUser)
    localStorage.setItem('artisanvault_user', JSON.stringify(authUser))
    localStorage.setItem('artisanvault_token', res.token)
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem('artisanvault_user')
    localStorage.removeItem('artisanvault_token')
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
