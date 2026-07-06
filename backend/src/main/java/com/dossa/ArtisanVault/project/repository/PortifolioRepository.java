package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.PortifolioRowMapper;
import com.dossa.ArtisanVault.project.entity.Portifolio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
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
        String sql = "SELECT * FROM portfolio WHERE id_portfolio = ?";
        return jdbcTemplate.queryForObject(sql, new PortifolioRowMapper(), id);
    }

    //Deletar portifolio por id
    public int deleteById(Long id){
        String sql = "DELETE FROM portfolio WHERE id_portfolio = ?";
        return jdbcTemplate.update(sql, id);
    }

    //Criar portifolio;
    public Portifolio save(Portifolio portifolio){
        String sql = "INSERT INTO portfolio (id_artista, titulo, descricao, imagem_url, id_cliente, id_pedido) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_portfolio"});
            ps.setLong(1, portifolio.getId_artista());
            ps.setString(2, portifolio.getTitulo());
            ps.setString(3, portifolio.getDescricao());
            ps.setString(4, portifolio.getImagem_url());
            if (portifolio.getId_cliente() != null) {
                ps.setLong(5, portifolio.getId_cliente());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            if (portifolio.getId_pedido() != null) {
                ps.setLong(6, portifolio.getId_pedido());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            return ps;
        }, keyHolder);
        portifolio.setId_portfolio(keyHolder.getKeyAs(Number.class).longValue());
        return portifolio;
    }
}
