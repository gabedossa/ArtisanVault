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

  async create(data: { titulo: string; descricao: string; imagem: File }): Promise<Portifolio> {
    const formData = new FormData()
    formData.append('titulo', data.titulo)
    formData.append('descricao', data.descricao)
    formData.append('imagem', data.imagem)

    const res = await api.post<Portifolio>('/portifolio', formData, {
      headers: { 'Content-Type': undefined },
    })
    return res.data
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/portifolio/delete/${id}`)
  },
}
