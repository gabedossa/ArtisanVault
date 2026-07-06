package com.dossa.ArtisanVault.project.RowMapper;

import com.dossa.ArtisanVault.project.entity.Portifolio;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PortifolioRowMapper implements RowMapper<Portifolio> {
    @Override
    public Portifolio mapRow(ResultSet rs, int rowNum) throws SQLException {
        Portifolio portfolio = new Portifolio();
        portfolio.setId_portfolio(rs.getLong("id_portfolio"));
        portfolio.setId_artista(rs.getLong("id_artista"));
        portfolio.setTitulo(rs.getString("titulo"));
        portfolio.setDescricao(rs.getString("descricao"));
        portfolio.setImagem_url(rs.getString("imagem_url"));

        return portfolio;
    }
}
