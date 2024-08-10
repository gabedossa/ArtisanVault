import React, { useState, useEffect } from "react";
import { getAllArtista } from "../../services/ArtistaService";
import './artistaTable.css';

// Por enquanto esse componente serve apenas para teste
export const ArtistaTable = () => {
  const [artistas, setArtistas] = useState([]);

  useEffect(() => {
    const fetchArtistas = async () => {
      try {
        const data = await getAllArtista();
        setArtistas(data);
      } catch (error) {
        console.error('Erro ao buscar artistas:', error);
      }
    };
    fetchArtistas();
  }, []);

  if (artistas.length === 0) {
    return <div><p style={{ color: "black" }}>Carregando artistas...</p></div>;
  }

  return (
    <div className="table_card">
      <h2>Lista de Artistas</h2>
      <ul>
        {artistas.map((artista) => (
            <div>
                <ul>
                    <tr className="userBlock" key={artista.idArtista}>Nome:{artista.nome} | E-mail:{artista.email} | Tipo: {artista.tipoUsuario}</tr>

                </ul>
            </div>
        ))}
      </ul>
    </div>
  );
}
