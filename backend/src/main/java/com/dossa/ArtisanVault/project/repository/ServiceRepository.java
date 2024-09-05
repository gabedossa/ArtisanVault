package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.ServicoRowMapper;
import com.dossa.ArtisanVault.project.entity.Portifolio;
import com.dossa.ArtisanVault.project.entity.Servico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

    //Deletar servico por id
    public int deleteById(Long id){
        String sql = "DELETE FROM setvico WHERE Id_servico = ?";
        return jdbcTemplate.update(sql, id);
    }

    //Criar servico;
    public int save(Servico servico){
        String sql = "INSERT INTO servico (Id_artista, descricao, valor_servico) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, servico.getId_artista(), servico.getDescricao(), servico.getValor_servico() );

    }

}
