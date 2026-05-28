'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { Palette, Users, ShoppingBag, BookImage, ArrowRight, Star } from 'lucide-react'
import { artistaService } from '@/lib/services/artista.service'
import { Artista } from '@/types'
import ArtistaCard from '@/components/ArtistaCard'

export default function HomePage() {
  const [artistas, setArtistas] = useState<Artista[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    artistaService
      .findAll()
      .then((data) => setArtistas(data.slice(0, 6)))
      .catch(() => setArtistas([]))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="flex flex-col">
      {/* Hero */}
      <section className="relative bg-linear-to-br from-violet-900 via-violet-800 to-purple-900 text-white overflow-hidden">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,var(--tw-gradient-stops))] from-amber-400/10 via-transparent to-transparent" />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-32 relative">
          <div className="max-w-2xl">
            <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm text-amber-300 text-xs font-semibold px-3 py-1.5 rounded-full mb-6 border border-white/10">
              <Star className="w-3 h-3" />
              Plataforma de Arte e Serviços Criativos
            </div>
            <h1 className="text-4xl md:text-6xl font-bold leading-tight mb-6">
              Conectando{' '}
              <span className="text-transparent bg-clip-text bg-linear-to-r from-amber-300 to-amber-500">
                Artistas
              </span>{' '}
              a Clientes
            </h1>
            <p className="text-violet-200 text-lg md:text-xl mb-10 leading-relaxed">
              Descubra talentos únicos, explore portfólios incríveis e contrate serviços
              criativos de artistas talentosos em um só lugar.
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <Link
                href="/artistas"
                className="flex items-center justify-center gap-2 bg-amber-400 hover:bg-amber-500 text-gray-900 font-bold px-8 py-3.5 rounded-xl transition-colors text-sm"
              >
                Explorar Artistas <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                href="/cadastro/artista"
                className="flex items-center justify-center gap-2 bg-white/10 hover:bg-white/20 border border-white/20 text-white font-semibold px-8 py-3.5 rounded-xl transition-colors text-sm backdrop-blur-sm"
              >
                Sou Artista
              </Link>
            </div>
          </div>
        </div>
        <div className="absolute bottom-0 left-0 right-0 h-16 bg-linear-to-t from-gray-50 to-transparent" />
      </section>

      {/* Features */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="text-center mb-14">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">Como funciona</h2>
          <p className="text-gray-500 max-w-xl mx-auto">Uma plataforma simples para conectar arte e pessoas.</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            { icon: Users, title: 'Cadastre-se', desc: 'Crie seu perfil como artista ou cliente em poucos minutos.', color: 'bg-violet-50 text-violet-600' },
            { icon: BookImage, title: 'Explore Portfólios', desc: 'Navegue por portfólios e obras de artistas de diversas áreas criativas.', color: 'bg-amber-50 text-amber-600' },
            { icon: ShoppingBag, title: 'Faça Pedidos', desc: 'Entre em contato com artistas e solicite serviços personalizados.', color: 'bg-green-50 text-green-600' },
          ].map(({ icon: Icon, title, desc, color }) => (
            <div key={title} className="bg-white rounded-2xl p-8 border border-gray-100 shadow-sm text-center">
              <div className={`inline-flex p-4 rounded-2xl ${color} mb-5`}>
                <Icon className="w-7 h-7" />
              </div>
              <h3 className="font-bold text-gray-900 text-lg mb-2">{title}</h3>
              <p className="text-gray-500 text-sm leading-relaxed">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Artistas em destaque */}
      <section className="bg-white border-y border-gray-100 py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-10">
            <div>
              <h2 className="text-3xl font-bold text-gray-900">Artistas em Destaque</h2>
              <p className="text-gray-500 mt-1">Conheça alguns dos talentos da plataforma</p>
            </div>
            <Link href="/artistas" className="hidden md:flex items-center gap-1 text-violet-700 font-semibold text-sm hover:gap-2 transition-all">
              Ver todos <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="bg-gray-100 rounded-2xl h-52 animate-pulse" />
              ))}
            </div>
          ) : artistas.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {artistas.map((a) => (
                <ArtistaCard key={a.idArtista} artista={a} />
              ))}
            </div>
          ) : (
            <div className="text-center py-16 text-gray-400">
              <Palette className="w-12 h-12 mx-auto mb-3 opacity-30" />
              <p>Nenhum artista cadastrado ainda.</p>
              <Link href="/cadastro/artista" className="mt-3 inline-block text-violet-700 font-medium text-sm">
                Seja o primeiro!
              </Link>
            </div>
          )}

          <div className="mt-8 text-center md:hidden">
            <Link href="/artistas" className="text-violet-700 font-semibold text-sm">
              Ver todos os artistas →
            </Link>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="bg-linear-to-br from-violet-700 to-purple-800 rounded-3xl p-10 md:p-16 text-white text-center">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">Pronto para começar?</h2>
          <p className="text-violet-200 mb-10 max-w-lg mx-auto">
            Junte-se a artistas e clientes que já usam a plataforma para criar conexões e trabalhos incríveis.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link href="/cadastro/artista" className="bg-amber-400 hover:bg-amber-500 text-gray-900 font-bold px-8 py-3.5 rounded-xl transition-colors text-sm">
              Cadastrar como Artista
            </Link>
            <Link href="/cadastro/cliente" className="bg-white/10 hover:bg-white/20 border border-white/20 text-white font-semibold px-8 py-3.5 rounded-xl transition-colors text-sm">
              Cadastrar como Cliente
            </Link>
          </div>
        </div>
      </section>
    </div>
  )
}
