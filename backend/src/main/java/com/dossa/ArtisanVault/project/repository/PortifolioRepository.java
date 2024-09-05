package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.PortifolioRowMapper;
import com.dossa.ArtisanVault.project.entity.Pedido;
import com.dossa.ArtisanVault.project.entity.Portifolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PortifolioRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Encontrar todos os portifólios no DB
    public List<Portifolio> findAll(){
        String sql = "SELECT * FROM portfolio";
        return jdbcTemplate.query(sql, new PortifolioRowMapper());
    }

    // Encontrar portifólio por ID;
    public Portifolio findById(Long id){
        String sql ="SELECT * FROM portifolio WHERE id_portifolio = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Portifolio.class), id);
    }

    //Deletar portifolio por id
    public int deleteById(Long id){
        String sql = "DELETE FROM portifolio WHERE Id_portifolio = ?";
        return jdbcTemplate.update(sql, id);
    }

    //Criar portifolio;
    public int save(Portifolio portifolio){
        String sql = "INSERT INTO pedido ( Id_artista, titulo, descricao) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, portifolio.getId_artista(), portifolio.getTitulo(),portifolio.getDescricao());

    }
}
