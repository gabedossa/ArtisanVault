'use client'

import { createContext, useContext, useEffect, useState, ReactNode } from 'react'
import { AuthUser, LoginRequest } from '@/types'
import { authService } from '@/lib/services/auth.service'
import { clienteService } from '@/lib/services/cliente.service'
import { artistaService } from '@/lib/services/artista.service'

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
    if (!stored) {
      setLoading(false)
      return
    }

    const storedUser: AuthUser = JSON.parse(stored)

    // O token vive apenas no cookie httpOnly; validamos a sessão contra o
    // backend em vez de confiar cegamente no que está no localStorage.
    const validate =
      storedUser.userType === 'CLIENTE'
        ? clienteService.me()
        : artistaService.findByEmail(storedUser.email)

    validate
      .then(() => setUser(storedUser))
      .catch(() => {
        localStorage.removeItem('artisanvault_user')
      })
      .finally(() => setLoading(false))
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
  }

  const logout = () => {
    authService.logout().catch(() => {})
    setUser(null)
    localStorage.removeItem('artisanvault_user')
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
