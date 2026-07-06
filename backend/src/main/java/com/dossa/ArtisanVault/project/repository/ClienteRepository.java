package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.ArtistaRowMapper;
import com.dossa.ArtisanVault.project.RowMapper.ClienteRowMapper;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClienteRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Encontrar todos os Clientes no DB
    public List<Cliente> findAll() {
        String sql = "SELECT * FROM cliente";
        return jdbcTemplate.query(sql, new ClienteRowMapper());
    }

    // Encontrar Cliente por ID
    public Cliente findById(Long id) {
        String sql = "SELECT * FROM cliente WHERE id_cliente = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Cliente.class), id);
    }

    // Encontrar Cliente por Email
    public Optional<Cliente> findByEmail(String email) {
        String sql = "SELECT * FROM cliente WHERE LOWER(email) = ?";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, new Object[]{email}, new ClienteRowMapper());
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Criar Cliente
    public int save(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome, tipo_usuario, email, senha, telefone) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, cliente.getNome(), cliente.getTipoUsuario(), cliente.getEmail(), cliente.getSenha(), cliente.getTelefone());
    }

    // Atualizar Cliente
    public int update(Cliente cliente) {
        String sql = "UPDATE cliente SET nome=?, tipo_usuario=?, email=?, senha=?, telefone=? WHERE id_cliente = ?";
        return jdbcTemplate.update(sql, cliente.getNome(), cliente.getTipoUsuario(), cliente.getEmail(), cliente.getSenha(), cliente.getTelefone(), cliente.getIdCliente());
    }

    // Excluir Cliente
    public int deleteById(Long id) {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";
        return jdbcTemplate.update(sql, id);
    }

}