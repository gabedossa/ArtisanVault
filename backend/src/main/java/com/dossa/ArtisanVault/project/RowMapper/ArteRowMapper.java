package com.dossa.ArtisanVault.project.RowMapper;

import com.dossa.ArtisanVault.project.entity.Arte;
import com.dossa.ArtisanVault.project.entity.Cliente;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArteRowMapper implements RowMapper<Arte> {
    @Override
    public Arte mapRow(ResultSet rs, int rowNum) throws SQLException {
        Arte arte = new Arte();
        arte.setId_arte(rs.getLong("id_arte"));
        arte.setId_portfolio(rs.getLong("id_portfolio"));
        arte.setTitulo(rs.getString("titulo"));
        arte.setDescricao(rs.getString("descricao"));
        arte.setVote(rs.getLong("vote"));
        return arte;
    }
}
