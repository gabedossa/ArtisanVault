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

  async create(data: { titulo: string; descricao: string; valor_servico: number }): Promise<Servico> {
    const res = await api.post<Servico>('/servico', data)
    return res.data
  },

  async update(id: number, data: { titulo: string; descricao: string; valor_servico: number }): Promise<Servico> {
    const res = await api.put<Servico>(`/servico/${id}`, data)
    return res.data
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/servico/delete/${id}`)
  },
}
