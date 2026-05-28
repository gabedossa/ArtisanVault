import api from '../api'
import { Servico } from '@/types'

export const servicoService = {
  async findAll(): Promise<Servico[]> {
    const res = await api.get<Servico[]>('/servico')
    return res.data
  },

  async findByArtista(idArtista: number): Promise<Servico[]> {
    const all = await servicoService.findAll()
    return all.filter((s) => s.id_artista === idArtista)
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/servico/delete/${id}`)
  },
}
