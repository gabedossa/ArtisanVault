package com.dossa.ArtisanVault.project.RowMapper;

import com.dossa.ArtisanVault.project.entity.Servico;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ServicoRowMapper implements RowMapper<Servico> {
    public Servico mapRow(ResultSet rs, int rowNum) throws SQLException {
        Servico servico = new Servico();
        servico.setId_servico(rs.getLong("id_servico"));
        servico.setId_artista(rs.getLong("id_artista"));
        servico.setTitulo(rs.getString("titulo"));
        servico.setDescricao(rs.getString("descricao"));
        servico.setValor_servico(rs.getDouble("valor_servico"));

        return servico;
    }
}
