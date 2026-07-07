package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.ArteRowMapper;
import com.dossa.ArtisanVault.project.entity.Arte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ArteRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Encontrar todas as artes no DB
    public List<Arte> findAll(){
        String sql = "SELECT * FROM arte";
        return jdbcTemplate.query(sql, new ArteRowMapper());
    }

    // Encontrar Arte por ID;
    public Arte findById(Long id){
        String sql ="SELECT * FROM arte WHERE id_arte = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new ArteRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Encontrar Arte por titulo;
    public Arte findByTitulo(String titulo){
        String sql ="SELECT * FROM arte WHERE titulo = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Arte.class), titulo);
    }

    //Deletar arte por id
    public int deleteById(Long id){
        String sql = "DELETE FROM arte WHERE Id_arte = ?";
        return jdbcTemplate.update(sql, id);
    }

    //Criar arte;
    public int save(Arte arte){
        String sql = "INSERT INTO arte (id_portfolio, titulo, descricao, vote) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, arte.getId_portfolio(), arte.getTitulo(),arte.getDescricao(), arte.getVote());

    }
}
