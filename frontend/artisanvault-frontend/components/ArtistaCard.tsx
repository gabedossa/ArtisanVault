import { Artista } from '@/types'
import { User, Mail, ArrowRight } from 'lucide-react'
import Link from 'next/link'

interface Props {
  artista: Artista
}

export default function ArtistaCard({ artista }: Props) {
  const initials = artista.nome
    .split(' ')
    .map((n) => n[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow overflow-hidden group">
      <div className="bg-gradient-to-br from-violet-500 to-purple-700 h-24 flex items-center justify-center">
        <div className="w-14 h-14 rounded-full bg-white/20 flex items-center justify-center text-white font-bold text-xl">
          {initials}
        </div>
      </div>
      <div className="p-5">
        <h3 className="font-bold text-gray-900 text-lg">{artista.nome}</h3>
        <div className="flex items-center gap-1 text-gray-500 text-xs mt-1 mb-3">
          <Mail className="w-3 h-3" />
          {artista.email}
        </div>
        {artista.descricao && (
          <p className="text-gray-600 text-sm line-clamp-2 mb-4">{artista.descricao}</p>
        )}
        <Link
          href={`/artistas/${artista.idArtista}`}
          className="flex items-center gap-1 text-violet-700 text-sm font-semibold hover:gap-2 transition-all"
        >
          Ver perfil <ArrowRight className="w-4 h-4" />
        </Link>
      </div>
    </div>
  )
}
