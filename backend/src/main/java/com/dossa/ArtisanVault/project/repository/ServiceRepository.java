package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.ServicoRowMapper;
import com.dossa.ArtisanVault.project.entity.Servico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class ServiceRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Encontrar todos os servicos no DB
    public List<Servico> findAll(){
        String sql = "SELECT * FROM servico";
        return jdbcTemplate.query(sql, new ServicoRowMapper());
    }

    //Encontrar servico por ID
    public Servico findById(Long id){
        String sql = "SELECT * FROM servico WHERE id_servico = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new ServicoRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    //Deletar servico por id
    public int deleteById(Long id){
        String sql = "DELETE FROM servico WHERE id_servico = ?";
        return jdbcTemplate.update(sql, id);
    }

    //Criar servico;
    public Servico save(Servico servico){
        String sql = "INSERT INTO servico (id_artista, titulo, descricao, valor_servico) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_servico"});
            ps.setLong(1, servico.getId_artista());
            ps.setString(2, servico.getTitulo());
            ps.setString(3, servico.getDescricao());
            ps.setDouble(4, servico.getValor_servico());
            return ps;
        }, keyHolder);
        servico.setId_servico(keyHolder.getKeyAs(Number.class).longValue());
        return servico;
    }

    //Atualizar servico;
    public int update(Servico servico){
        String sql = "UPDATE servico SET titulo = ?, descricao = ?, valor_servico = ? WHERE id_servico = ?";
        return jdbcTemplate.update(sql, servico.getTitulo(), servico.getDescricao(), servico.getValor_servico(), servico.getId_servico());
    }
}
