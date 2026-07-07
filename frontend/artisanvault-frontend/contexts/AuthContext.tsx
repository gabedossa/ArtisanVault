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

function toAuthUser(res: { email: string; userType: 'ARTISTA' | 'CLIENTE'; userId: number; nome: string }): AuthUser {
  return {
    email: res.email,
    userType: res.userType,
    userId: res.userId,
    userName: res.nome,
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // O token vive apenas no cookie httpOnly; a identidade da sessão é
    // sempre derivada do backend, nunca de algo salvo no navegador.
    authService
      .me()
      .then((res) => setUser(toAuthUser(res)))
      .catch(() => setUser(null))
      .finally(() => setLoading(false))
  }, [])

  const login = async (data: LoginRequest) => {
    const res = await authService.login(data)
    setUser(toAuthUser(res))
  }

  const logout = () => {
    authService.logout().catch(() => {})
    setUser(null)
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
