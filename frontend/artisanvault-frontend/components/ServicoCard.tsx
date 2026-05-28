import { Servico } from '@/types'
import { Wrench, DollarSign, Trash2 } from 'lucide-react'

interface Props {
  servico: Servico
  onDelete?: (id: number) => void
  isOwner?: boolean
}

export default function ServicoCard({ servico, onDelete, isOwner }: Props) {
  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 flex-1">
          <div className="bg-amber-50 dark:bg-amber-900/30 p-2 rounded-lg">
            <Wrench className="w-5 h-5 text-amber-600 dark:text-amber-400" />
          </div>
          <div className="flex-1">
            <p className="text-gray-800 dark:text-gray-200 text-sm leading-relaxed">{servico.descricao}</p>
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <div className="flex items-center gap-1 bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 font-bold text-sm px-3 py-1 rounded-lg">
            <DollarSign className="w-3 h-3" />
            {Number(servico.valor_servico).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
          </div>
          {isOwner && onDelete && (
            <button
              onClick={() => onDelete(servico.id_servico)}
              className="text-red-400 hover:text-red-600 transition-colors p-1"
              title="Remover serviço"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
