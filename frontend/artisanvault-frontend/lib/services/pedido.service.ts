import api from '../api'
import { Pedido, Portifolio } from '@/types'

export const pedidoService = {
  async findAll(): Promise<Pedido[]> {
    const res = await api.get<Pedido[]>('/pedido')
    return res.data
  },

  async findById(id: number): Promise<Pedido> {
    const res = await api.get<Pedido>(`/pedido/${id}`)
    return res.data
  },

  async findByCliente(idCliente: number): Promise<Pedido[]> {
    const all = await pedidoService.findAll()
    return all.filter((p) => p.id_cliente === idCliente)
  },

  async findByArtista(idArtista: number): Promise<Pedido[]> {
    const all = await pedidoService.findAll()
    return all.filter((p) => p.id_artista === idArtista)
  },

  async create(data: { id_servico: number; descricao: string }): Promise<Pedido> {
    const res = await api.post<Pedido>('/pedido', data)
    return res.data
  },

  async iniciar(id: number): Promise<Pedido> {
    const res = await api.put<Pedido>(`/pedido/${id}/iniciar`)
    return res.data
  },

  async entregar(
    id: number,
    data: { titulo: string; descricao: string; imagem: File }
  ): Promise<{ pedido: Pedido; trabalho: Portifolio }> {
    const formData = new FormData()
    formData.append('titulo', data.titulo)
    formData.append('descricao', data.descricao)
    formData.append('imagem', data.imagem)

    const res = await api.post<{ pedido: Pedido; trabalho: Portifolio }>(
      `/pedido/${id}/entregar`,
      formData,
      { headers: { 'Content-Type': undefined } }
    )
    return res.data
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/pedido/delete/${id}`)
  },
}
