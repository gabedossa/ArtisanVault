'use client'

import { useEffect, useState } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import { useRouter } from 'next/navigation'
import { artistaService } from '@/lib/services/artista.service'
import { portifolioService } from '@/lib/services/portifolio.service'
import { servicoService } from '@/lib/services/servico.service'
import { pedidoService } from '@/lib/services/pedido.service'
import { Artista, Portifolio, Servico, Pedido } from '@/types'
import PortifolioCard from '@/components/PortifolioCard'
import ServicoCard from '@/components/ServicoCard'
import PedidoCard from '@/components/PedidoCard'
import {
  User, Mail, FileText, BookImage, Wrench, ShoppingBag,
  Plus, Save, Edit2, AlertCircle
} from 'lucide-react'

type Tab = 'perfil' | 'portifolios' | 'servicos' | 'pedidos'

export default function DashboardArtistaPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const router = useRouter()

  const [artista, setArtista] = useState<Artista | null>(null)
  const [portifolios, setPortifolios] = useState<Portifolio[]>([])
  const [servicos, setServicos] = useState<Servico[]>([])
  const [pedidos, setPedidos] = useState<Pedido[]>([])
  const [activeTab, setActiveTab] = useState<Tab>('perfil')
  const [pageLoading, setPageLoading] = useState(true)

  // Perfil edit
  const [editMode, setEditMode] = useState(false)
  const [editForm, setEditForm] = useState({ nome: '', descricao: '' })
  const [saving, setSaving] = useState(false)
  const [saveMsg, setSaveMsg] = useState('')

  // Novo portfólio
  const [showAddPortifolio, setShowAddPortifolio] = useState(false)
  const [newPortifolio, setNewPortifolio] = useState({ titulo: '', descricao: '' })

  // Novo serviço
  const [showAddServico, setShowAddServico] = useState(false)
  const [newServico, setNewServico] = useState({ descricao: '', valor_servico: '' })

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || user?.userType !== 'ARTISTA')) {
      router.push('/login')
    }
  }, [authLoading, isAuthenticated, user, router])

  useEffect(() => {
    if (!user?.userId) return
    const id = user.userId
    Promise.all([
      artistaService.findById(id),
      portifolioService.findByArtista(id),
      servicoService.findByArtista(id),
      pedidoService.findByArtista(id),
    ])
      .then(([a, p, s, ped]) => {
        setArtista(a)
        setEditForm({ nome: a.nome, descricao: a.descricao ?? '' })
        setPortifolios(p)
        setServicos(s)
        setPedidos(ped)
      })
      .finally(() => setPageLoading(false))
  }, [user?.userId])

  const handleSavePerfil = async () => {
    if (!artista) return
    setSaving(true)
    try {
      const payload = {
        idArtista: artista.idArtista,
        nome: editForm.nome,
        descricao: editForm.descricao ?? '',
        email: artista.email,
        senha: artista.senha ?? '',
        tipoUsuario: artista.tipoUsuario ?? 'ARTISTA',
      }
      const updated = await artistaService.update(artista.idArtista, payload)
      setArtista(updated)
      setEditMode(false)
      setSaveMsg('Perfil atualizado!')
      setTimeout(() => setSaveMsg(''), 3000)
    } finally {
      setSaving(false)
    }
  }

  const handleDeletePortifolio = async (id: number) => {
    if (!confirm('Remover este portfólio?')) return
    await portifolioService.delete(id)
    setPortifolios((prev) => prev.filter((p) => p.id_portfolio !== id))
  }

  const handleDeleteServico = async (id: number) => {
    if (!confirm('Remover este serviço?')) return
    await servicoService.delete(id)
    setServicos((prev) => prev.filter((s) => s.id_servico !== id))
  }

  const handleDeletePedido = async (id: number) => {
    if (!confirm('Remover este pedido?')) return
    await pedidoService.delete(id)
    setPedidos((prev) => prev.filter((p) => p.id_pedido !== id))
  }

  const handleAddServico = async (e: { preventDefault(): void }) => {
    e.preventDefault()
    if (!user?.userId) return
    setSaving(true)
    try {
      // NOTE: Backend has no POST /servico endpoint; call goes through but may not persist
      const created = await fetch('http://localhost:8080/api/servico', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          id_artista: user.userId,
          descricao: newServico.descricao,
          valor_servico: parseFloat(newServico.valor_servico),
        }),
      }).then((r) => r.json())
      setServicos((prev) => [...prev, created])
      setNewServico({ descricao: '', valor_servico: '' })
      setShowAddServico(false)
    } finally {
      setSaving(false)
    }
  }

  const handleAddPortifolio = async (e: { preventDefault(): void }) => {
    e.preventDefault()
    if (!user?.userId) return
    setSaving(true)
    try {
      const created = await fetch('http://localhost:8080/api/portifolio', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          id_artista: user.userId,
          titulo: newPortifolio.titulo,
          descricao: newPortifolio.descricao,
        }),
      }).then((r) => r.json())
      setPortifolios((prev) => [...prev, created])
      setNewPortifolio({ titulo: '', descricao: '' })
      setShowAddPortifolio(false)
    } finally {
      setSaving(false)
    }
  }

  if (authLoading || pageLoading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-12 space-y-4">
        {[...Array(3)].map((_, i) => <div key={i} className="bg-gray-100 rounded-2xl h-24 animate-pulse" />)}
      </div>
    )
  }

  if (!artista) return null

  const tabs: { key: Tab; label: string; icon: React.ElementType; count?: number }[] = [
    { key: 'perfil', label: 'Meu Perfil', icon: User },
    { key: 'portifolios', label: 'Portfólios', icon: BookImage, count: portifolios.length },
    { key: 'servicos', label: 'Serviços', icon: Wrench, count: servicos.length },
    { key: 'pedidos', label: 'Pedidos', icon: ShoppingBag, count: pedidos.length },
  ]

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      {/* Header */}
      <div className="mb-8">
        <p className="text-sm text-violet-700 font-semibold mb-1">Dashboard</p>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Olá, {artista?.nome?.split(' ')[0] ?? ''}!</h1>
        <p className="text-gray-500 text-sm mt-1">Gerencie seu perfil, portfólios e serviços</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4 mb-8">
        {[
          { label: 'Portfólios', value: portifolios.length, color: 'bg-violet-50 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300', icon: BookImage },
          { label: 'Serviços', value: servicos.length, color: 'bg-amber-50 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300', icon: Wrench },
          { label: 'Pedidos', value: pedidos.length, color: 'bg-green-50 text-green-700 dark:bg-green-900/30 dark:text-green-300', icon: ShoppingBag },
        ].map(({ label, value, color, icon: Icon }) => (
          <div key={label} className="bg-white dark:bg-gray-800 rounded-xl border border-gray-100 dark:border-gray-700 p-5 shadow-sm">
            <div className={`inline-flex p-2 rounded-lg ${color} mb-3`}>
              <Icon className="w-4 h-4" />
            </div>
            <div className="text-2xl font-bold text-gray-900 dark:text-white">{value}</div>
            <div className="text-sm text-gray-500 dark:text-gray-400">{label}</div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm overflow-hidden">
        <div className="flex border-b border-gray-100 dark:border-gray-700 overflow-x-auto">
          {tabs.map(({ key, label, icon: Icon, count }) => (
            <button
              key={key}
              onClick={() => setActiveTab(key)}
              className={`flex items-center gap-2 px-5 py-4 text-sm font-medium whitespace-nowrap transition-colors border-b-2 ${
                activeTab === key
                  ? 'border-violet-700 text-violet-700 bg-violet-50/50 dark:bg-violet-900/20'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50 dark:hover:text-gray-300 dark:hover:bg-gray-700'
              }`}
            >
              <Icon className="w-4 h-4" />
              {label}
              {count !== undefined && (
                <span className={`text-xs px-1.5 py-0.5 rounded-full ${activeTab === key ? 'bg-violet-100 text-violet-700' : 'bg-gray-100 text-gray-500'}`}>
                  {count}
                </span>
              )}
            </button>
          ))}
        </div>

        <div className="p-6">
          {/* TAB: PERFIL */}
          {activeTab === 'perfil' && (
            <div className="max-w-lg">
              {saveMsg && (
                <div className="flex items-center gap-2 bg-green-50 text-green-700 text-sm px-4 py-3 rounded-lg mb-4 border border-green-100">
                  <Save className="w-4 h-4" /> {saveMsg}
                </div>
              )}
              <div className="space-y-5">
                <div>
                  <label className="flex items-center gap-1 text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    <User className="w-3.5 h-3.5" /> Nome
                  </label>
                  {editMode ? (
                    <input
                      value={editForm.nome}
                      onChange={(e) => setEditForm({ ...editForm, nome: e.target.value })}
                      className="w-full px-4 py-2.5 border border-gray-200 dark:border-gray-600 rounded-lg text-sm text-gray-900 dark:text-white dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-violet-500"
                    />
                  ) : (
                    <p className="text-gray-900 dark:text-white bg-gray-50 dark:bg-gray-700 px-4 py-2.5 rounded-lg text-sm">{artista.nome}</p>
                  )}
                </div>
                <div>
                  <label className="flex items-center gap-1 text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    <Mail className="w-3.5 h-3.5" /> Email
                  </label>
                  <p className="text-gray-500 dark:text-gray-400 bg-gray-50 dark:bg-gray-700 px-4 py-2.5 rounded-lg text-sm">{artista.email}</p>
                </div>
                <div>
                  <label className="flex items-center gap-1 text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                    <FileText className="w-3.5 h-3.5" /> Descrição
                  </label>
                  {editMode ? (
                    <textarea
                      value={editForm.descricao}
                      onChange={(e) => setEditForm({ ...editForm, descricao: e.target.value })}
                      rows={4}
                      className="w-full px-4 py-2.5 border border-gray-200 dark:border-gray-600 rounded-lg text-sm text-gray-900 dark:text-white dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-violet-500 resize-none"
                    />
                  ) : (
                    <p className="text-gray-700 dark:text-gray-200 bg-gray-50 dark:bg-gray-700 px-4 py-2.5 rounded-lg text-sm min-h-20">
                      {artista.descricao || 'Nenhuma descrição.'}
                    </p>
                  )}
                </div>
                <div className="flex gap-3">
                  {editMode ? (
                    <>
                      <button onClick={handleSavePerfil} disabled={saving} className="flex items-center gap-1 bg-violet-700 text-white text-sm font-semibold px-5 py-2 rounded-lg hover:bg-violet-800 transition-colors disabled:opacity-50">
                        <Save className="w-4 h-4" />
                        {saving ? 'Salvando...' : 'Salvar'}
                      </button>
                      <button onClick={() => setEditMode(false)} className="text-gray-500 text-sm px-5 py-2 rounded-lg hover:bg-gray-100 transition-colors">
                        Cancelar
                      </button>
                    </>
                  ) : (
                    <button onClick={() => setEditMode(true)} className="flex items-center gap-1 border border-gray-200 dark:border-gray-600 text-gray-700 dark:text-white text-sm font-medium px-5 py-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                      <Edit2 className="w-4 h-4" /> Editar Perfil
                    </button>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* TAB: PORTFÓLIOS */}
          {activeTab === 'portifolios' && (
            <div>
              <div className="flex items-center justify-between mb-5">
                <p className="text-sm text-gray-500">{portifolios.length} portfólio(s)</p>
                <button
                  onClick={() => setShowAddPortifolio(!showAddPortifolio)}
                  className="flex items-center gap-1 bg-violet-700 text-white text-sm font-semibold px-4 py-2 rounded-lg hover:bg-violet-800 transition-colors"
                >
                  <Plus className="w-4 h-4" /> Novo Portfólio
                </button>
              </div>

              {showAddPortifolio && (
                <form onSubmit={handleAddPortifolio} className="bg-violet-50 rounded-xl border border-violet-100 p-5 mb-5 space-y-3">
                  <h4 className="font-semibold text-gray-900 text-sm">Novo Portfólio</h4>
                  <div className="flex items-center gap-2 bg-amber-50 text-amber-700 text-xs px-3 py-2 rounded-lg border border-amber-100">
                    <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                    O backend ainda não possui endpoint POST para portfólios. Esta funcionalidade estará disponível após a correção do backend.
                  </div>
                  <input
                    required
                    value={newPortifolio.titulo}
                    onChange={(e) => setNewPortifolio({ ...newPortifolio, titulo: e.target.value })}
                    placeholder="Título do portfólio"
                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 bg-white"
                  />
                  <textarea
                    value={newPortifolio.descricao}
                    onChange={(e) => setNewPortifolio({ ...newPortifolio, descricao: e.target.value })}
                    placeholder="Descrição"
                    rows={2}
                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500 resize-none bg-white"
                  />
                  <div className="flex gap-2">
                    <button type="submit" disabled={saving} className="bg-violet-700 text-white text-sm font-semibold px-5 py-2 rounded-lg hover:bg-violet-800 disabled:opacity-50">
                      {saving ? 'Salvando...' : 'Salvar'}
                    </button>
                    <button type="button" onClick={() => setShowAddPortifolio(false)} className="text-gray-500 text-sm px-5 py-2 rounded-lg hover:bg-gray-100">
                      Cancelar
                    </button>
                  </div>
                </form>
              )}

              {portifolios.length > 0 ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {portifolios.map((p) => (
                    <PortifolioCard key={p.id_portfolio} portifolio={p} isOwner onDelete={handleDeletePortifolio} />
                  ))}
                </div>
              ) : (
                <div className="text-center py-12 text-gray-400">
                  <BookImage className="w-10 h-10 mx-auto mb-3 opacity-20" />
                  <p className="text-sm">Nenhum portfólio ainda.</p>
                </div>
              )}
            </div>
          )}

          {/* TAB: SERVIÇOS */}
          {activeTab === 'servicos' && (
            <div>
              <div className="flex items-center justify-between mb-5">
                <p className="text-sm text-gray-500">{servicos.length} serviço(s)</p>
                <button
                  onClick={() => setShowAddServico(!showAddServico)}
                  className="flex items-center gap-1 bg-amber-500 text-white text-sm font-semibold px-4 py-2 rounded-lg hover:bg-amber-600 transition-colors"
                >
                  <Plus className="w-4 h-4" /> Novo Serviço
                </button>
              </div>

              {showAddServico && (
                <form onSubmit={handleAddServico} className="bg-amber-50 rounded-xl border border-amber-100 p-5 mb-5 space-y-3">
                  <h4 className="font-semibold text-gray-900 text-sm">Novo Serviço</h4>
                  <div className="flex items-center gap-2 bg-amber-50 text-amber-700 text-xs px-3 py-2 rounded-lg border border-amber-200">
                    <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                    O backend ainda não possui endpoint POST para serviços. Esta funcionalidade estará disponível após a correção do backend.
                  </div>
                  <textarea
                    required
                    value={newServico.descricao}
                    onChange={(e) => setNewServico({ ...newServico, descricao: e.target.value })}
                    placeholder="Descreva o serviço"
                    rows={2}
                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 resize-none bg-white"
                  />
                  <input
                    required
                    type="number"
                    min="0"
                    step="0.01"
                    value={newServico.valor_servico}
                    onChange={(e) => setNewServico({ ...newServico, valor_servico: e.target.value })}
                    placeholder="Valor (R$)"
                    className="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 bg-white"
                  />
                  <div className="flex gap-2">
                    <button type="submit" disabled={saving} className="bg-amber-500 text-white text-sm font-semibold px-5 py-2 rounded-lg hover:bg-amber-600 disabled:opacity-50">
                      {saving ? 'Salvando...' : 'Salvar'}
                    </button>
                    <button type="button" onClick={() => setShowAddServico(false)} className="text-gray-500 text-sm px-5 py-2 rounded-lg hover:bg-gray-100">
                      Cancelar
                    </button>
                  </div>
                </form>
              )}

              {servicos.length > 0 ? (
                <div className="space-y-3">
                  {servicos.map((s) => (
                    <ServicoCard key={s.id_servico} servico={s} isOwner onDelete={handleDeleteServico} />
                  ))}
                </div>
              ) : (
                <div className="text-center py-12 text-gray-400">
                  <Wrench className="w-10 h-10 mx-auto mb-3 opacity-20" />
                  <p className="text-sm">Nenhum serviço cadastrado.</p>
                </div>
              )}
            </div>
          )}

          {/* TAB: PEDIDOS */}
          {activeTab === 'pedidos' && (
            <div>
              <p className="text-sm text-gray-500 mb-5">{pedidos.length} pedido(s) recebido(s)</p>
              {pedidos.length > 0 ? (
                <div className="space-y-3">
                  {pedidos.map((p) => (
                    <PedidoCard key={p.id_pedido} pedido={p} isOwner onDelete={handleDeletePedido} />
                  ))}
                </div>
              ) : (
                <div className="text-center py-12 text-gray-400">
                  <ShoppingBag className="w-10 h-10 mx-auto mb-3 opacity-20" />
                  <p className="text-sm">Nenhum pedido recebido.</p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
