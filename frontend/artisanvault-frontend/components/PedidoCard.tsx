import { Pedido } from '@/types'
import { ShoppingBag, Calendar, CheckCircle, Clock, Loader, Trash2, Upload, Play } from 'lucide-react'
import { API_ORIGIN } from '@/lib/api'

interface Props {
  pedido: Pedido
  onDelete?: (id: number) => void
  onDeliver?: (pedido: Pedido) => void
  onIniciar?: (pedido: Pedido) => void
  isOwner?: boolean
}

export default function PedidoCard({ pedido, onDelete, onDeliver, onIniciar, isOwner }: Props) {
  const status = pedido.entregue
    ? { label: 'Entregue', color: 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400', icon: CheckCircle }
    : pedido.trabalhando
    ? { label: 'Em andamento', color: 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400', icon: Loader }
    : { label: 'Aguardando', color: 'bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400', icon: Clock }

  const StatusIcon = status.icon

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 flex-1">
          {pedido.imagem_url ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={`${API_ORIGIN}${pedido.imagem_url}`}
              alt="Arte entregue"
              className="w-14 h-14 rounded-lg object-cover shrink-0"
            />
          ) : (
            <div className="bg-violet-50 dark:bg-violet-900/30 p-2 rounded-lg shrink-0">
              <ShoppingBag className="w-5 h-5 text-violet-600 dark:text-violet-400" />
            </div>
          )}
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <span className="font-semibold text-gray-900 dark:text-white text-sm">Pedido #{pedido.id_pedido}</span>
              <span className={`flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full ${status.color}`}>
                <StatusIcon className="w-3 h-3" />
                {status.label}
              </span>
            </div>
            {pedido.descricao && (
              <p className="text-gray-600 dark:text-gray-300 text-sm line-clamp-2 mb-2">{pedido.descricao}</p>
            )}
            <div className="flex items-center gap-4 text-xs text-gray-400 dark:text-gray-500">
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
        <div className="flex items-center gap-2 shrink-0">
          {isOwner && onIniciar && !pedido.trabalhando && !pedido.entregue && (
            <button
              onClick={() => onIniciar(pedido)}
              className="flex items-center gap-1 bg-blue-600 text-white text-xs font-semibold px-3 py-1.5 rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Play className="w-3.5 h-3.5" /> Iniciar trabalho
            </button>
          )}
          {isOwner && onDeliver && !pedido.entregue && (
            <button
              onClick={() => onDeliver(pedido)}
              className="flex items-center gap-1 bg-violet-700 text-white text-xs font-semibold px-3 py-1.5 rounded-lg hover:bg-violet-800 transition-colors"
            >
              <Upload className="w-3.5 h-3.5" /> Enviar arte
            </button>
          )}
          {isOwner && onDelete && (
            <button
              onClick={() => onDelete(pedido.id_pedido)}
              className="text-red-400 hover:text-red-600 transition-colors p-1"
              title="Cancelar pedido"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
