package com.dossa.ArtisanVault.project.RowMapper;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Cliente;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteRowMapper implements RowMapper<Cliente> {
    @Override
    public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getLong("id_cliente"));
        cliente.setNome(rs.getString("nome"));
        cliente.setEmail(rs.getString("email"));
        cliente.setSenha(rs.getString("senha"));
        cliente.setTelefone(rs.getString("telefone"));
        cliente.setTipoUsuario(rs.getString("tipo_usuario"));
        return cliente;
    }
}
