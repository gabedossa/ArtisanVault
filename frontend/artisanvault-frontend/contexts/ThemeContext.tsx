'use client'

import { createContext, useContext, useEffect, useState, ReactNode } from 'react'

type Theme = 'light' | 'dark'

interface ThemeContextType {
  theme: Theme
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextType>({} as ThemeContextType)

export function ThemeProvider({ children }: { children: ReactNode }) {
  // Sempre inicia em 'light', igual ao servidor (que não tem acesso a
  // localStorage), para o primeiro paint do cliente bater com o HTML
  // renderizado no servidor. O tema real só é lido depois de montado, no
  // efeito abaixo — ler localStorage direto no useState faria o cliente
  // divergir do servidor e quebrar a hidratação.
  const [theme, setTheme] = useState<Theme>('light')

  useEffect(() => {
    const saved = localStorage.getItem('theme') as Theme | null
    if (saved) {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- sincroniza com localStorage (só existe no cliente), não dá para ler no initializer sem quebrar a hidratação
      setTheme(saved)
    }
  }, [])

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark')
  }, [theme])

  const toggleTheme = () => {
    const next: Theme = theme === 'light' ? 'dark' : 'light'
    setTheme(next)
    localStorage.setItem('theme', next)
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme() {
  return useContext(ThemeContext)
}
