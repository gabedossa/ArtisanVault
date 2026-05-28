import { Pedido } from '@/types'
import { ShoppingBag, Calendar, CheckCircle, Clock, Loader, Trash2 } from 'lucide-react'

interface Props {
  pedido: Pedido
  onDelete?: (id: number) => void
  isOwner?: boolean
}

export default function PedidoCard({ pedido, onDelete, isOwner }: Props) {
  const status = pedido.entregue
    ? { label: 'Entregue', color: 'bg-green-100 text-green-700', icon: CheckCircle }
    : pedido.trabalhando
    ? { label: 'Em andamento', color: 'bg-blue-100 text-blue-700', icon: Loader }
    : { label: 'Aguardando', color: 'bg-amber-100 text-amber-700', icon: Clock }

  const StatusIcon = status.icon

  return (
    <div className="bg-white rounded-xl border border-gray-100 p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 flex-1">
          <div className="bg-violet-50 p-2 rounded-lg shrink-0">
            <ShoppingBag className="w-5 h-5 text-violet-600" />
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <span className="font-semibold text-gray-900 text-sm">Pedido #{pedido.id_pedido}</span>
              <span className={`flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full ${status.color}`}>
                <StatusIcon className="w-3 h-3" />
                {status.label}
              </span>
            </div>
            {pedido.descricao && (
              <p className="text-gray-600 text-sm line-clamp-2 mb-2">{pedido.descricao}</p>
            )}
            <div className="flex items-center gap-4 text-xs text-gray-400">
              {pedido.dt_pedido && (
                <span className="flex items-center gap-1">
                  <Calendar className="w-3 h-3" />
                  Pedido: {new Date(pedido.dt_pedido).toLocaleDateString('pt-BR')}
                </span>
              )}
              {pedido.dt_previsao_entrega && (
                <span className="flex items-center gap-1">
                  <Calendar className="w-3 h-3" />
                  Entrega: {new Date(pedido.dt_previsao_entrega).toLocaleDateString('pt-BR')}
                </span>
              )}
            </div>
          </div>
        </div>
        {isOwner && onDelete && (
          <button
            onClick={() => onDelete(pedido.id_pedido)}
            className="text-red-400 hover:text-red-600 transition-colors p-1 shrink-0"
            title="Cancelar pedido"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  )
}
