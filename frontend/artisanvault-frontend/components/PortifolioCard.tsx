import { Portifolio } from '@/types'
import { Trash2, ArrowRight } from 'lucide-react'
import { API_ORIGIN } from '@/lib/api'
import Link from 'next/link'

interface Props {
  portifolio: Portifolio
  onDelete?: (id: number) => void
  isOwner?: boolean
}

export default function PortifolioCard({ portifolio, onDelete, isOwner }: Props) {
  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 shadow-sm hover:shadow-md transition-shadow group overflow-hidden">
      <div className="relative">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={`${API_ORIGIN}${portifolio.imagem_url}`}
          alt={portifolio.titulo}
          className="w-full h-40 object-cover bg-gray-100 dark:bg-gray-700"
        />
        {isOwner && onDelete && (
          <button
            onClick={() => onDelete(portifolio.id_portfolio)}
            className="absolute top-2 right-2 bg-black/50 hover:bg-black/70 text-white rounded-lg p-1.5 transition-colors"
            title="Remover trabalho"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        )}
      </div>
      <div className="p-5">
        <h4 className="font-semibold text-gray-900 dark:text-white mb-1">{portifolio.titulo}</h4>
        {portifolio.descricao && (
          <p className="text-gray-500 dark:text-gray-400 text-sm line-clamp-2 mb-3">{portifolio.descricao}</p>
        )}
        <Link
          href={`/portifolios/${portifolio.id_portfolio}`}
          className="flex items-center gap-1 text-violet-700 dark:text-violet-400 text-sm font-medium hover:gap-2 transition-all"
        >
          Ver trabalho <ArrowRight className="w-3 h-3" />
        </Link>
      </div>
    </div>
  )
}
