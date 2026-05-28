import api from '../api'
import { Cliente } from '@/types'

export const clienteService = {
  async findAll(): Promise<Cliente[]> {
    const res = await api.get<Cliente[]>('/cliente')
    return res.data
  },

  async findById(id: number): Promise<Cliente> {
    const res = await api.get<Cliente>(`/cliente/${id}`)
    return res.data
  },

  async findByEmail(email: string): Promise<Cliente | null> {
    const all = await clienteService.findAll()
    return all.find((c) => c.email.toLowerCase() === email.toLowerCase()) ?? null
  },

  async create(cliente: Omit<Cliente, 'idCliente'>): Promise<Cliente> {
    const res = await api.post<Cliente>('/cliente/post', cliente)
    return res.data
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/cliente/delete/${id}`)
  },
}
