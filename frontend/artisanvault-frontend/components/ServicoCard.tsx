'use client'

import { useState } from 'react'
import { Servico } from '@/types'
import { Wrench, DollarSign, Trash2, Edit2, Send, Check } from 'lucide-react'

interface Props {
  servico: Servico
  onDelete?: (id: number) => void
  onEdit?: (servico: Servico) => void
  isOwner?: boolean
  onRequest?: (servico: Servico, mensagem: string) => Promise<void>
}

export default function ServicoCard({ servico, onDelete, onEdit, isOwner, onRequest }: Props) {
  const [requesting, setRequesting] = useState(false)
  const [mensagem, setMensagem] = useState('')
  const [sending, setSending] = useState(false)
  const [sent, setSent] = useState(false)

  const handleSendRequest = async () => {
    if (!onRequest) return
    setSending(true)
    try {
      await onRequest(servico, mensagem)
      setSent(true)
      setRequesting(false)
      setMensagem('')
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 flex-1">
          <div className="bg-amber-50 dark:bg-amber-900/30 p-2 rounded-lg">
            <Wrench className="w-5 h-5 text-amber-600 dark:text-amber-400" />
          </div>
          <div className="flex-1">
            {servico.titulo && (
              <h4 className="font-semibold text-gray-900 dark:text-white text-sm mb-1">{servico.titulo}</h4>
            )}
            <p className="text-gray-600 dark:text-gray-300 text-sm leading-relaxed">{servico.descricao}</p>
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <div className="flex items-center gap-1 bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 font-bold text-sm px-3 py-1 rounded-lg">
            <DollarSign className="w-3 h-3" />
            {Number(servico.valor_servico).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
          </div>
          {isOwner && onEdit && (
            <button
              onClick={() => onEdit(servico)}
              className="text-gray-400 hover:text-violet-600 transition-colors p-1"
              title="Editar serviço"
            >
              <Edit2 className="w-4 h-4" />
            </button>
          )}
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

      {onRequest && !sent && (
        <div className="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
          {requesting ? (
            <div className="space-y-2">
              <textarea
                value={mensagem}
                onChange={(e) => setMensagem(e.target.value)}
                placeholder="Descreva o que você precisa (opcional)"
                rows={2}
                className="w-full px-3 py-2 border border-gray-200 dark:border-gray-600 rounded-lg text-sm text-gray-900 dark:text-white dark:placeholder:text-gray-400 bg-white dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-violet-500 resize-none"
              />
              <div className="flex gap-2">
                <button
                  onClick={handleSendRequest}
                  disabled={sending}
                  className="flex items-center gap-1 bg-violet-700 text-white text-xs font-semibold px-3 py-1.5 rounded-lg hover:bg-violet-800 disabled:opacity-50"
                >
                  <Send className="w-3 h-3" /> {sending ? 'Enviando...' : 'Enviar pedido'}
                </button>
                <button
                  onClick={() => setRequesting(false)}
                  className="text-gray-500 dark:text-gray-400 text-xs px-3 py-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  Cancelar
                </button>
              </div>
            </div>
          ) : (
            <button
              onClick={() => setRequesting(true)}
              className="flex items-center gap-1 text-violet-700 dark:text-violet-400 text-xs font-semibold hover:gap-2 transition-all"
            >
              <Send className="w-3 h-3" /> Solicitar este serviço
            </button>
          )}
        </div>
      )}

      {sent && (
        <div className="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700 flex items-center gap-1 text-green-600 dark:text-green-400 text-xs font-medium">
          <Check className="w-3.5 h-3.5" /> Pedido enviado! Acompanhe no seu painel.
        </div>
      )}
    </div>
  )
}
