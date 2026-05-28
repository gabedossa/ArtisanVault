import api from '../api'
import { Portifolio } from '@/types'

export const portifolioService = {
  async findAll(): Promise<Portifolio[]> {
    const res = await api.get<Portifolio[]>('/portifolio')
    return res.data
  },

  async findById(id: number): Promise<Portifolio> {
    const res = await api.get<Portifolio>(`/portifolio/${id}`)
    return res.data
  },

  async findByArtista(idArtista: number): Promise<Portifolio[]> {
    const all = await portifolioService.findAll()
    return all.filter((p) => p.id_artista === idArtista)
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/portifolio/delete/${id}`)
  },
}
