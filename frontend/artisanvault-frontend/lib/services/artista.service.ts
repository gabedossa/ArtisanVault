import api from '../api'
import { Artista } from '@/types'

export const artistaService = {
  async findAll(): Promise<Artista[]> {
    const res = await api.get<Artista[]>('/artistas')
    return res.data
  },

  async findById(id: number): Promise<Artista> {
    const res = await api.get<Artista>(`/artistas/${id}`)
    return res.data
  },

  async findByEmail(email: string): Promise<Artista> {
    const res = await api.get<Artista>('/artistas/email', { params: { email } })
    return res.data
  },

  async create(artista: Omit<Artista, 'idArtista'>): Promise<Artista> {
    const res = await api.post<Artista>('/artistas', artista)
    return res.data
  },

  async update(id: number, artista: Partial<Artista>): Promise<Artista> {
    const res = await api.put<Artista>(`/artistas/${id}`, artista)
    return res.data
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/artistas/${id}`)
  },

  async login(email: string, senha: string): Promise<string> {
    const res = await api.post<string>('/artistas/login', null, {
      params: { email, senha },
    })
    return res.data
  },
}
