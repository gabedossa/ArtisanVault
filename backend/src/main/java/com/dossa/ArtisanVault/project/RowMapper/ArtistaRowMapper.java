package com.dossa.ArtisanVault.project.RowMapper;

import com.dossa.ArtisanVault.project.entity.Artista;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistaRowMapper implements RowMapper<Artista> {
    @Override
    public Artista mapRow(ResultSet rs, int rowNum) throws SQLException {
        Artista artista = new Artista();
        artista.setIdArtista(rs.getLong("id_artista"));
        artista.setNome(rs.getString("nome"));
        artista.setDescricao(rs.getString("descricao"));
        artista.setEmail(rs.getString("email"));
        artista.setSenha(rs.getString("senha"));
        artista.setTipoUsuario(rs.getString("tipo_usuario"));
        return artista;
    }
}
