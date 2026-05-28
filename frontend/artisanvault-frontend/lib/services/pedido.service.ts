import api from '../api'
import { Pedido } from '@/types'

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

  async delete(id: number): Promise<void> {
    await api.delete(`/pedido/delete/${id}`)
  },
}
