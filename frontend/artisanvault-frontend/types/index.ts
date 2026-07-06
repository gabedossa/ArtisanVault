export interface Artista {
  idArtista: number
  nome: string
  descricao: string
  email: string
  senha?: string
  tipoUsuario: string
}

export interface Cliente {
  idCliente: number
  nome: string
  email: string
  senha?: string
  telefone: string
  tipoUsuario: string
}

export interface Servico {
  id_servico: number
  id_artista: number
  titulo: string
  descricao: string
  valor_servico: number
}

export interface Portifolio {
  id_portfolio: number
  id_artista: number
  titulo: string
  descricao: string
  imagem_url?: string
  id_cliente?: number
  id_pedido?: number
}

export interface Arte {
  id_arte: number
  id_portfolio: number
  titulo: string
  descricao: string
  vote: number
}

export interface Pedido {
  id_pedido: number
  id_cliente: number
  id_artista: number
  id_servico: number
  id_arte: number
  descricao: string
  dt_pedido: string
  dt_previsao_entrega: string
  entregue: boolean
  trabalhando: boolean
  id_portfolio?: number
  imagem_url?: string
}

export interface LoginRequest {
  email: string
  senha: string
}

export interface LoginResponse {
  email: string
  userType: 'ARTISTA' | 'CLIENTE'
  userId: number
  nome: string
  token: string
}

export interface AuthUser {
  email: string
  userType: 'ARTISTA' | 'CLIENTE'
  userId: number | null
  userName: string | null
}
