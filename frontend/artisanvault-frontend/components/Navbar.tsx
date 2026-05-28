'use client'

import { useAuth } from '@/contexts/AuthContext'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { Palette, LogOut, User, LayoutDashboard } from 'lucide-react'
import { useState } from 'react'

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth()
  const router = useRouter()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    router.push('/')
  }

  const dashboardPath =
    user?.userType === 'ARTISTA' ? '/dashboard/artista' : '/dashboard/cliente'

  return (
    <nav className="bg-white border-b border-gray-100 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <Link href="/" className="flex items-center gap-2 font-bold text-xl text-violet-700">
            <Palette className="w-6 h-6" />
            ArtisanVault
          </Link>

          <div className="hidden md:flex items-center gap-6">
            <Link href="/artistas" className="text-gray-600 hover:text-violet-700 transition-colors text-sm font-medium">
              Artistas
            </Link>
            {isAuthenticated ? (
              <>
                <Link
                  href={dashboardPath}
                  className="flex items-center gap-1 text-gray-600 hover:text-violet-700 transition-colors text-sm font-medium"
                >
                  <LayoutDashboard className="w-4 h-4" />
                  Dashboard
                </Link>
                <div className="flex items-center gap-3">
                  <span className="text-sm text-gray-500 flex items-center gap-1">
                    <User className="w-4 h-4" />
                    {user?.userName}
                  </span>
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-1 text-sm text-red-500 hover:text-red-700 transition-colors font-medium"
                  >
                    <LogOut className="w-4 h-4" />
                    Sair
                  </button>
                </div>
              </>
            ) : (
              <div className="flex items-center gap-3">
                <Link
                  href="/login"
                  className="text-sm font-medium text-violet-700 hover:text-violet-900 transition-colors"
                >
                  Entrar
                </Link>
                <Link
                  href="/cadastro/artista"
                  className="bg-violet-700 text-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-violet-800 transition-colors"
                >
                  Cadastrar
                </Link>
              </div>
            )}
          </div>

          <button
            className="md:hidden p-2 rounded-lg text-gray-600 hover:bg-gray-100"
            onClick={() => setMenuOpen(!menuOpen)}
          >
            <div className="w-5 h-0.5 bg-current mb-1" />
            <div className="w-5 h-0.5 bg-current mb-1" />
            <div className="w-5 h-0.5 bg-current" />
          </button>
        </div>
      </div>

      {menuOpen && (
        <div className="md:hidden border-t border-gray-100 bg-white px-4 py-3 space-y-2">
          <Link href="/artistas" className="block py-2 text-gray-600 text-sm" onClick={() => setMenuOpen(false)}>
            Artistas
          </Link>
          {isAuthenticated ? (
            <>
              <Link href={dashboardPath} className="block py-2 text-gray-600 text-sm" onClick={() => setMenuOpen(false)}>
                Dashboard
              </Link>
              <button onClick={handleLogout} className="block py-2 text-red-500 text-sm">
                Sair
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="block py-2 text-violet-700 text-sm" onClick={() => setMenuOpen(false)}>
                Entrar
              </Link>
              <Link href="/cadastro/artista" className="block py-2 text-violet-700 text-sm font-medium" onClick={() => setMenuOpen(false)}>
                Cadastrar
              </Link>
            </>
          )}
        </div>
      )}
    </nav>
  )
}
