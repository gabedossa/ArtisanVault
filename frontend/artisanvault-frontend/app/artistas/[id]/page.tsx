'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import { artistaService } from '@/lib/services/artista.service'
import { portifolioService } from '@/lib/services/portifolio.service'
import { servicoService } from '@/lib/services/servico.service'
import { Artista, Portifolio, Servico } from '@/types'
import ServicoCard from '@/components/ServicoCard'
import PortifolioCard from '@/components/PortifolioCard'
import { Mail, ArrowLeft, Palette } from 'lucide-react'
import Link from 'next/link'

export default function ArtistaProfilePage() {
  const { id } = useParams<{ id: string }>()
  const [artista, setArtista] = useState<Artista | null>(null)
  const [portifolios, setPortifolios] = useState<Portifolio[]>([])
  const [servicos, setServicos] = useState<Servico[]>([])
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)

  useEffect(() => {
    const artistaId = parseInt(id)
    Promise.all([
      artistaService.findById(artistaId),
      portifolioService.findByArtista(artistaId),
      servicoService.findByArtista(artistaId),
    ])
      .then(([a, p, s]) => {
        setArtista(a)
        setPortifolios(p)
        setServicos(s)
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false))
  }, [id])

  const initials = artista?.nome
    .split(' ')
    .map((n) => n[0])
    .slice(0, 2)
    .join('')
    .toUpperCase() ?? ''

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 space-y-4">
        <div className="bg-gray-100 rounded-2xl h-40 animate-pulse" />
        <div className="bg-gray-100 rounded-2xl h-32 animate-pulse" />
      </div>
    )
  }

  if (notFound || !artista) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-24 text-center text-gray-400">
        <Palette className="w-14 h-14 mx-auto mb-4 opacity-20" />
        <p className="text-lg font-medium">Artista não encontrado</p>
        <Link href="/artistas" className="mt-4 inline-block text-violet-700 text-sm font-medium">
          ← Ver todos os artistas
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <Link href="/artistas" className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-violet-700 mb-8 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Voltar
      </Link>

      {/* Header do perfil */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mb-8">
        <div className="bg-linear-to-br from-violet-500 to-purple-700 h-28" />
        <div className="px-6 pb-6">
          <div className="flex items-end gap-5 -mt-10 mb-4">
            <div className="w-20 h-20 rounded-2xl bg-white border-4 border-white shadow-md flex items-center justify-center text-violet-700 font-bold text-2xl bg-violet-100">
              {initials}
            </div>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">{artista.nome}</h1>
          <div className="flex items-center gap-1 text-gray-500 text-sm mt-1 mb-4">
            <Mail className="w-4 h-4" />
            {artista.email}
          </div>
          {artista.descricao && (
            <p className="text-gray-600 leading-relaxed max-w-2xl">{artista.descricao}</p>
          )}
        </div>
      </div>

      {/* Serviços */}
      <section className="mb-8">
        <h2 className="text-xl font-bold text-gray-900 mb-4">
          Serviços{' '}
          <span className="text-sm font-normal text-gray-400">({servicos.length})</span>
        </h2>
        {servicos.length > 0 ? (
          <div className="space-y-3">
            {servicos.map((s) => (
              <ServicoCard key={s.id_servico} servico={s} />
            ))}
          </div>
        ) : (
          <p className="text-gray-400 text-sm py-6 text-center bg-white rounded-xl border border-gray-100">
            Nenhum serviço cadastrado.
          </p>
        )}
      </section>

      {/* Portfólios */}
      <section>
        <h2 className="text-xl font-bold text-gray-900 mb-4">
          Portfólios{' '}
          <span className="text-sm font-normal text-gray-400">({portifolios.length})</span>
        </h2>
        {portifolios.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {portifolios.map((p) => (
              <PortifolioCard key={p.id_portfolio} portifolio={p} />
            ))}
          </div>
        ) : (
          <p className="text-gray-400 text-sm py-6 text-center bg-white rounded-xl border border-gray-100">
            Nenhum portfólio cadastrado.
          </p>
        )}
      </section>
    </div>
  )
}
