import { Portifolio } from '@/types'
import { BookImage, Trash2, ArrowRight } from 'lucide-react'
import Link from 'next/link'

interface Props {
  portifolio: Portifolio
  onDelete?: (id: number) => void
  isOwner?: boolean
}

export default function PortifolioCard({ portifolio, onDelete, isOwner }: Props) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 p-5 shadow-sm hover:shadow-md transition-shadow group">
      <div className="flex items-start justify-between gap-2">
        <div className="bg-violet-50 p-2 rounded-lg">
          <BookImage className="w-5 h-5 text-violet-600" />
        </div>
        {isOwner && onDelete && (
          <button
            onClick={() => onDelete(portifolio.id_portfolio)}
            className="text-red-400 hover:text-red-600 transition-colors p-1"
            title="Remover portfólio"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        )}
      </div>
      <h4 className="font-semibold text-gray-900 mt-3 mb-1">{portifolio.titulo}</h4>
      {portifolio.descricao && (
        <p className="text-gray-500 text-sm line-clamp-2 mb-3">{portifolio.descricao}</p>
      )}
      <Link
        href={`/portifolios/${portifolio.id_portfolio}`}
        className="flex items-center gap-1 text-violet-700 text-sm font-medium hover:gap-2 transition-all"
      >
        Ver obras <ArrowRight className="w-3 h-3" />
      </Link>
    </div>
  )
}
