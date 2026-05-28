'use client'

import { useEffect, useState } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import { useRouter } from 'next/navigation'
import { clienteService } from '@/lib/services/cliente.service'
import { pedidoService } from '@/lib/services/pedido.service'
import { Cliente, Pedido } from '@/types'
import PedidoCard from '@/components/PedidoCard'
import { ShoppingBag, User, Mail, Phone, Users, ArrowRight } from 'lucide-react'
import Link from 'next/link'

type Tab = 'pedidos' | 'perfil'

export default function DashboardClientePage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const router = useRouter()

  const [cliente, setCliente] = useState<Cliente | null>(null)
  const [pedidos, setPedidos] = useState<Pedido[]>([])
  const [activeTab, setActiveTab] = useState<Tab>('pedidos')
  const [pageLoading, setPageLoading] = useState(true)

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || user?.userType !== 'CLIENTE')) {
      router.push('/login')
    }
  }, [authLoading, isAuthenticated, user, router])

  useEffect(() => {
    if (!user?.userId) return
    Promise.all([
      clienteService.findById(user.userId),
      pedidoService.findByCliente(user.userId),
    ])
      .then(([c, ped]) => {
        setCliente(c)
        setPedidos(ped)
      })
      .finally(() => setPageLoading(false))
  }, [user?.userId])

  const handleDeletePedido = async (id: number) => {
    if (!confirm('Cancelar este pedido?')) return
    await pedidoService.delete(id)
    setPedidos((prev) => prev.filter((p) => p.id_pedido !== id))
  }

  const entregues = pedidos.filter((p) => p.entregue).length
  const emAndamento = pedidos.filter((p) => p.trabalhando && !p.entregue).length
  const aguardando = pedidos.filter((p) => !p.trabalhando && !p.entregue).length

  if (authLoading || pageLoading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12 space-y-4">
        {[...Array(3)].map((_, i) => <div key={i} className="bg-gray-100 rounded-2xl h-24 animate-pulse" />)}
      </div>
    )
  }

  if (!cliente) return null

  const tabs: { key: Tab; label: string; icon: React.ElementType; count?: number }[] = [
    { key: 'pedidos', label: 'Meus Pedidos', icon: ShoppingBag, count: pedidos.length },
    { key: 'perfil', label: 'Meu Perfil', icon: User },
  ]

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="mb-8">
        <p className="text-sm text-amber-600 font-semibold mb-1">Dashboard</p>
        <h1 className="text-3xl font-bold text-gray-900">Olá, {cliente.nome.split(' ')[0]}!</h1>
        <p className="text-gray-500 text-sm mt-1">Acompanhe seus pedidos e explore artistas</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4 mb-8">
        {[
          { label: 'Aguardando', value: aguardando, color: 'bg-amber-50 text-amber-700' },
          { label: 'Em andamento', value: emAndamento, color: 'bg-blue-50 text-blue-700' },
          { label: 'Entregues', value: entregues, color: 'bg-green-50 text-green-700' },
        ].map(({ label, value, color }) => (
          <div key={label} className="bg-white rounded-xl border border-gray-100 p-5 shadow-sm">
            <div className={`inline-block text-xs font-semibold px-2 py-1 rounded-full ${color} mb-2`}>{label}</div>
            <div className="text-2xl font-bold text-gray-900">{value}</div>
          </div>
        ))}
      </div>

      {/* Explorar artistas CTA */}
      <Link
        href="/artistas"
        className="flex items-center justify-between bg-linear-to-br from-violet-700 to-purple-800 text-white rounded-2xl p-5 mb-8 hover:opacity-95 transition-opacity"
      >
        <div className="flex items-center gap-3">
          <div className="bg-white/10 p-2.5 rounded-xl">
            <Users className="w-5 h-5" />
          </div>
          <div>
            <p className="font-bold">Explorar Artistas</p>
            <p className="text-violet-200 text-sm">Descubra novos talentos e serviços</p>
          </div>
        </div>
        <ArrowRight className="w-5 h-5" />
      </Link>

      {/* Tabs */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="flex border-b border-gray-100">
          {tabs.map(({ key, label, icon: Icon, count }) => (
            <button
              key={key}
              onClick={() => setActiveTab(key)}
              className={`flex items-center gap-2 px-5 py-4 text-sm font-medium whitespace-nowrap transition-colors border-b-2 ${
                activeTab === key
                  ? 'border-amber-500 text-amber-600 bg-amber-50/50'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
              }`}
            >
              <Icon className="w-4 h-4" />
              {label}
              {count !== undefined && (
                <span className={`text-xs px-1.5 py-0.5 rounded-full ${activeTab === key ? 'bg-amber-100 text-amber-700' : 'bg-gray-100 text-gray-500'}`}>
                  {count}
                </span>
              )}
            </button>
          ))}
        </div>

        <div className="p-6">
          {/* TAB: PEDIDOS */}
          {activeTab === 'pedidos' && (
            <div>
              {pedidos.length > 0 ? (
                <div className="space-y-3">
                  {pedidos.map((p) => (
                    <PedidoCard key={p.id_pedido} pedido={p} isOwner onDelete={handleDeletePedido} />
                  ))}
                </div>
              ) : (
                <div className="text-center py-16 text-gray-400">
                  <ShoppingBag className="w-12 h-12 mx-auto mb-4 opacity-20" />
                  <p className="font-medium">Nenhum pedido ainda</p>
                  <p className="text-sm mt-1">Explore artistas e faça seu primeiro pedido</p>
                  <Link
                    href="/artistas"
                    className="mt-4 inline-block bg-violet-700 text-white text-sm font-semibold px-5 py-2.5 rounded-xl hover:bg-violet-800 transition-colors"
                  >
                    Explorar Artistas
                  </Link>
                </div>
              )}
            </div>
          )}

          {/* TAB: PERFIL */}
          {activeTab === 'perfil' && (
            <div className="max-w-sm space-y-4">
              {[
                { icon: User, label: 'Nome', value: cliente.nome },
                { icon: Mail, label: 'Email', value: cliente.email },
                { icon: Phone, label: 'Telefone', value: cliente.telefone },
              ].map(({ icon: Icon, label, value }) => (
                <div key={label}>
                  <label className="flex items-center gap-1 text-sm font-medium text-gray-700 mb-1.5">
                    <Icon className="w-3.5 h-3.5" /> {label}
                  </label>
                  <p className="text-gray-800 bg-gray-50 px-4 py-2.5 rounded-lg text-sm">{value || '—'}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
