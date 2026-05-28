'use client'

import { useEffect, useState } from 'react'
import { artistaService } from '@/lib/services/artista.service'
import { Artista } from '@/types'
import ArtistaCard from '@/components/ArtistaCard'
import { Users, Search, Palette } from 'lucide-react'

export default function ArtistasPage() {
  const [artistas, setArtistas] = useState<Artista[]>([])
  const [filtered, setFiltered] = useState<Artista[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    artistaService
      .findAll()
      .then((data) => { setArtistas(data); setFiltered(data) })
      .catch(() => setArtistas([]))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    const q = search.toLowerCase()
    setFiltered(
      artistas.filter(
        (a) =>
          a.nome.toLowerCase().includes(q) ||
          a.email.toLowerCase().includes(q) ||
          (a.descricao && a.descricao.toLowerCase().includes(q))
      )
    )
  }, [search, artistas])

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="mb-10">
        <div className="flex items-center gap-2 text-violet-700 dark:text-violet-400 font-semibold text-sm mb-2">
          <Users className="w-4 h-4" />
          Todos os Artistas
        </div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">Explore Talentos</h1>
        <div className="relative max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome ou descrição..."
            className="w-full pl-10 pr-4 py-2.5 border border-gray-200 dark:border-gray-700 dark:bg-gray-800 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 placeholder:text-gray-400"
          />
        </div>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(9)].map((_, i) => (
            <div key={i} className="bg-gray-100 dark:bg-gray-800 rounded-2xl h-52 animate-pulse" />
          ))}
        </div>
      ) : filtered.length > 0 ? (
        <>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">{filtered.length} artista(s) encontrado(s)</p>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filtered.map((a) => <ArtistaCard key={a.idArtista} artista={a} />)}
          </div>
        </>
      ) : (
        <div className="text-center py-24 text-gray-400 dark:text-gray-600">
          <Palette className="w-14 h-14 mx-auto mb-4 opacity-20" />
          <p className="text-lg font-medium">Nenhum artista encontrado</p>
          <p className="text-sm mt-1">Tente uma busca diferente</p>
        </div>
      )}
    </div>
  )
}
