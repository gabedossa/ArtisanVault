import api from '../api'
import { Arte } from '@/types'

export const arteService = {
  async findAll(): Promise<Arte[]> {
    const res = await api.get<Arte[]>('/arte')
    return res.data
  },

  async findById(id: number): Promise<Arte> {
    const res = await api.get<Arte>(`/arte/${id}`)
    return res.data
  },

  async findByPortifolio(idPortifolio: number): Promise<Arte[]> {
    const all = await arteService.findAll()
    return all.filter((a) => a.id_portfolio === idPortifolio)
  },

  async create(arte: Omit<Arte, 'id_arte'>): Promise<Arte> {
    const res = await api.post<Arte>('/arte/post', arte)
    return res.data
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/arte/delete/${id}`)
  },
}
