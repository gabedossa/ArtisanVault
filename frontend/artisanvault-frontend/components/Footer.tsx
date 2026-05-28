import { Palette } from 'lucide-react'
import Link from 'next/link'

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-400 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <div className="flex items-center gap-2 text-white font-bold text-lg mb-3">
              <Palette className="w-5 h-5 text-violet-400" />
              ArtisanVault
            </div>
            <p className="text-sm leading-relaxed">
              Conectando artistas talentosos a clientes que valorizam arte e criatividade.
            </p>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-3 text-sm">Explorar</h4>
            <ul className="space-y-2 text-sm">
              <li><Link href="/artistas" className="hover:text-violet-400 transition-colors">Ver Artistas</Link></li>
              <li><Link href="/login" className="hover:text-violet-400 transition-colors">Entrar</Link></li>
              <li><Link href="/cadastro/artista" className="hover:text-violet-400 transition-colors">Sou Artista</Link></li>
              <li><Link href="/cadastro/cliente" className="hover:text-violet-400 transition-colors">Sou Cliente</Link></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-3 text-sm">Plataforma</h4>
            <ul className="space-y-2 text-sm">
              <li><span>Portfólios digitais</span></li>
              <li><span>Gestão de pedidos</span></li>
              <li><span>Vitrine de serviços</span></li>
            </ul>
          </div>
        </div>
        <div className="border-t border-gray-800 mt-8 pt-6 text-center text-xs">
          © {new Date().getFullYear()} ArtisanVault. Todos os direitos reservados.
        </div>
      </div>
    </footer>
  )
}
