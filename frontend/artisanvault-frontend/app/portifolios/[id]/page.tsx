'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import { portifolioService } from '@/lib/services/portifolio.service'
import { arteService } from '@/lib/services/arte.service'
import { Portifolio, Arte } from '@/types'
import { BookImage, ArrowLeft, Palette, Heart, Trash2, Plus } from 'lucide-react'
import Link from 'next/link'
import { useAuth } from '@/contexts/AuthContext'

export default function PortifolioPage() {
  const { id } = useParams<{ id: string }>()
  const { user } = useAuth()
  const [portifolio, setPortifolio] = useState<Portifolio | null>(null)
  const [artes, setArtes] = useState<Arte[]>([])
  const [loading, setLoading] = useState(true)
  const [showAddArte, setShowAddArte] = useState(false)
  const [newArte, setNewArte] = useState({ titulo: '', descricao: '' })
  const [saving, setSaving] = useState(false)

  const portfolioId = parseInt(id)

  useEffect(() => {
    Promise.all([
      portifolioService.findById(portfolioId),
      arteService.findByPortifolio(portfolioId),
    ])
      .then(([p, a]) => {
        setPortifolio(p)
        setArtes(a)
      })
      .finally(() => setLoading(false))
  }, [portfolioId])

  const isOwner =
    user?.userType === 'ARTISTA' && portifolio?.id_artista === user?.userId

  const handleAddArte = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    try {
      const created = await arteService.create({
        ...newArte,
        id_portfolio: portfolioId,
        vote: 0,
      })
      setArtes((prev) => [...prev, created])
      setNewArte({ titulo: '', descricao: '' })
      setShowAddArte(false)
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteArte = async (arteId: number) => {
    if (!confirm('Remover esta obra?')) return
    await arteService.delete(arteId)
    setArtes((prev) => prev.filter((a) => a.id_arte !== arteId))
  }

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 space-y-4">
        <div className="bg-gray-100 rounded-2xl h-24 animate-pulse" />
        <div className="grid grid-cols-2 gap-4">
          {[...Array(4)].map((_, i) => <div key={i} className="bg-gray-100 rounded-xl h-40 animate-pulse" />)}
        </div>
      </div>
    )
  }

  if (!portifolio) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-24 text-center text-gray-400">
        <Palette className="w-14 h-14 mx-auto mb-4 opacity-20" />
        <p>Portfólio não encontrado.</p>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <Link href="/artistas" className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-violet-700 mb-8 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Voltar
      </Link>

      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-8">
        <div className="flex items-start justify-between">
          <div className="flex items-start gap-4">
            <div className="bg-violet-100 p-3 rounded-xl">
              <BookImage className="w-6 h-6 text-violet-700" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{portifolio.titulo}</h1>
              {portifolio.descricao && (
                <p className="text-gray-500 mt-1 text-sm">{portifolio.descricao}</p>
              )}
            </div>
          </div>
          {isOwner && (
            <button
              onClick={() => setShowAddArte(!showAddArte)}
              className="flex items-center gap-1 bg-violet-700 text-white text-sm font-semibold px-4 py-2 rounded-lg hover:bg-violet-800 transition-colors"
            >
              <Plus className="w-4 h-4" />
              Adicionar Obra
            </button>
          )}
        </div>
      </div>

      {/* Formulário nova obra */}
      {showAddArte && (
        <form onSubmit={handleAddArte} className="bg-violet-50 rounded-2xl border border-violet-100 p-6 mb-8 space-y-4">
          <h3 className="font-semibold text-gray-900">Nova Obra</h3>
          <input
            required
            value={newArte.titulo}
            onChange={(e) => setNewArte({ ...newArte, titulo: e.target.value })}
            placeholder="Título da obra"
            className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 bg-white"
          />
          <textarea
            value={newArte.descricao}
            onChange={(e) => setNewArte({ ...newArte, descricao: e.target.value })}
            placeholder="Descrição da obra"
            rows={3}
            className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 resize-none bg-white"
          />
          <div className="flex gap-3">
            <button type="submit" disabled={saving} className="bg-violet-700 text-white text-sm font-semibold px-5 py-2 rounded-lg hover:bg-violet-800 transition-colors disabled:opacity-50">
              {saving ? 'Salvando...' : 'Salvar Obra'}
            </button>
            <button type="button" onClick={() => setShowAddArte(false)} className="text-gray-500 text-sm px-5 py-2 rounded-lg hover:bg-gray-100 transition-colors">
              Cancelar
            </button>
          </div>
        </form>
      )}

      {/* Obras */}
      <h2 className="text-xl font-bold text-gray-900 mb-4">
        Obras{' '}
        <span className="text-sm font-normal text-gray-400">({artes.length})</span>
      </h2>

      {artes.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {artes.map((arte) => (
            <div key={arte.id_arte} className="bg-white rounded-xl border border-gray-100 p-5 shadow-sm hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between mb-2">
                <h4 className="font-semibold text-gray-900">{arte.titulo}</h4>
                <div className="flex items-center gap-2">
                  <span className="flex items-center gap-1 text-pink-500 text-xs">
                    <Heart className="w-3 h-3 fill-current" /> {arte.vote}
                  </span>
                  {isOwner && (
                    <button onClick={() => handleDeleteArte(arte.id_arte)} className="text-red-400 hover:text-red-600 transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
              {arte.descricao && <p className="text-gray-500 text-sm leading-relaxed">{arte.descricao}</p>}
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-16 text-gray-400 bg-white rounded-xl border border-gray-100">
          <Palette className="w-10 h-10 mx-auto mb-3 opacity-20" />
          <p className="text-sm">Nenhuma obra neste portfólio ainda.</p>
          {isOwner && (
            <button onClick={() => setShowAddArte(true)} className="mt-3 text-violet-700 text-sm font-medium">
              Adicionar primeira obra
            </button>
          )}
        </div>
      )}
    </div>
  )
}
