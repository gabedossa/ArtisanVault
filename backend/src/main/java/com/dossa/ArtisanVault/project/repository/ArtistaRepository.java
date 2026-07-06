package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.ArtistaRowMapper;
import com.dossa.ArtisanVault.project.entity.Artista;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ArtistaRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Encontrar todos os artistas no DB
    public List<Artista> findAll(){
        String sql = "SELECT * FROM artista";
        return jdbcTemplate.query(sql, new ArtistaRowMapper());
    }

    // Encontrar Artista por ID
    public Artista findById(Long id){
        String sql ="SELECT * FROM artista WHERE id_artista = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Artista.class), id);
    }

    // Criar Artista
    public int save(Artista artista){
        String sql = "INSERT INTO artista (nome, descricao, email, senha, tipo_usuario) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, artista.getNome(), artista.getDescricao(), artista.getEmail(), artista.getSenha(), artista.getTipoUsuario());
    }

    // Atualizar Artista
    public int update(Artista artista){
        String sql = "UPDATE artista SET nome = ?, descricao = ?, email = ?, senha = ?, tipo_usuario = ? WHERE id_artista = ?";
        return jdbcTemplate.update(sql, artista.getNome(), artista.getDescricao(), artista.getEmail(), artista.getSenha(), artista.getTipoUsuario(), artista.getIdArtista());
    }

    // Excluir artista
    public int deleteById(Long id){
        String sql = "DELETE FROM artista WHERE id_artista = ?";
        return jdbcTemplate.update(sql, id);
    }

    // Método para encontrar Artista por email
    public Optional<Artista> findByEmail(String email) {
        String sql = "SELECT * FROM artista WHERE email = ?";
        try {
            Artista artista = jdbcTemplate.queryForObject(sql, new Object[]{email}, new ArtistaRowMapper());
            return Optional.ofNullable(artista);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}