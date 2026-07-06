'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { portifolioService } from '@/lib/services/portifolio.service'
import { Portifolio } from '@/types'
import { ArrowLeft, Palette, Trash2 } from 'lucide-react'
import { API_ORIGIN } from '@/lib/api'
import Link from 'next/link'
import { useAuth } from '@/contexts/AuthContext'

export default function PortifolioPage() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const { user } = useAuth()
  const [portifolio, setPortifolio] = useState<Portifolio | null>(null)
  const [loading, setLoading] = useState(true)

  const portfolioId = parseInt(id)

  useEffect(() => {
    portifolioService.findById(portfolioId)
      .then(setPortifolio)
      .finally(() => setLoading(false))
  }, [portfolioId])

  const isOwner =
    user?.userType === 'ARTISTA' && portifolio?.id_artista === user?.userId

  const handleDelete = async () => {
    if (!confirm('Remover este trabalho?')) return
    await portifolioService.delete(portfolioId)
    router.push('/dashboard/artista')
  }

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-12 space-y-4">
        <div className="bg-gray-100 dark:bg-gray-800 rounded-2xl h-96 animate-pulse" />
      </div>
    )
  }

  if (!portifolio) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-24 text-center text-gray-400 dark:text-gray-500">
        <Palette className="w-14 h-14 mx-auto mb-4 opacity-20" />
        <p>Trabalho não encontrado.</p>
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <Link href="/artistas" className="inline-flex items-center gap-1 text-sm text-gray-500 dark:text-gray-400 hover:text-violet-700 dark:hover:text-violet-400 mb-8 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Voltar
      </Link>

      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={`${API_ORIGIN}${portifolio.imagem_url}`}
          alt={portifolio.titulo}
          className="w-full max-h-128 object-cover bg-gray-100 dark:bg-gray-700"
        />
        <div className="p-6">
          <div className="flex items-start justify-between gap-4">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{portifolio.titulo}</h1>
            {isOwner && (
              <button
                onClick={handleDelete}
                className="flex items-center gap-1 text-red-500 hover:text-red-600 text-sm font-medium shrink-0"
              >
                <Trash2 className="w-4 h-4" /> Remover
              </button>
            )}
          </div>
          {portifolio.descricao && (
            <p className="text-gray-600 dark:text-gray-300 mt-3 leading-relaxed">{portifolio.descricao}</p>
          )}
        </div>
      </div>
    </div>
  )
}
