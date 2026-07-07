import api from '../api'
import { Cliente } from '@/types'

export const clienteService = {
  async me(): Promise<Cliente> {
    const res = await api.get<Cliente>('/cliente/me')
    return res.data
  },

  async create(cliente: Omit<Cliente, 'idCliente'>): Promise<Cliente> {
    const res = await api.post<Cliente>('/cliente/post', cliente)
    return res.data
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/cliente/delete/${id}`)
  },
}
