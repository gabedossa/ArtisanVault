'use client'

import { createContext, useContext, useEffect, useState, ReactNode } from 'react'
import { AuthUser, LoginRequest } from '@/types'
import { authService } from '@/lib/services/auth.service'
import { artistaService } from '@/lib/services/artista.service'
import { clienteService } from '@/lib/services/cliente.service'

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
    if (stored) setUser(JSON.parse(stored))
    setLoading(false)
  }, [])

  const login = async (data: LoginRequest) => {
    const res = await authService.login(data)

    let userId: number | null = null
    let userName: string | null = null

    if (res.userType === 'ARTISTA') {
      const artista = await artistaService.findByEmail(res.email)
      userId = artista.idArtista
      userName = artista.nome
    } else {
      const cliente = await clienteService.findByEmail(res.email)
      if (cliente) {
        userId = cliente.idCliente
        userName = cliente.nome
      }
    }

    const authUser: AuthUser = { email: res.email, userType: res.userType, userId, userName }
    setUser(authUser)
    localStorage.setItem('artisanvault_user', JSON.stringify(authUser))
  }

  const logout = () => {
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
