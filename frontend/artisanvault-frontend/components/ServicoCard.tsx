import { Servico } from '@/types'
import { Wrench, DollarSign, Trash2 } from 'lucide-react'

interface Props {
  servico: Servico
  onDelete?: (id: number) => void
  isOwner?: boolean
}

export default function ServicoCard({ servico, onDelete, isOwner }: Props) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 flex-1">
          <div className="bg-amber-50 p-2 rounded-lg">
            <Wrench className="w-5 h-5 text-amber-600" />
          </div>
          <div className="flex-1">
            <p className="text-gray-800 text-sm leading-relaxed">{servico.descricao}</p>
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <div className="flex items-center gap-1 bg-green-50 text-green-700 font-bold text-sm px-3 py-1 rounded-lg">
            <DollarSign className="w-3 h-3" />
            {Number(servico.valor_servico).toLocaleString('pt-BR', {
              minimumFractionDigits: 2,
            })}
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
