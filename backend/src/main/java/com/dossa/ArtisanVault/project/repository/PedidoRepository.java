package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.ArteRowMapper;
import com.dossa.ArtisanVault.project.RowMapper.PedidoRowMapper;
import com.dossa.ArtisanVault.project.entity.Arte;
import com.dossa.ArtisanVault.project.entity.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PedidoRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Encontrar todos os pedidos no DB
    public List<Pedido> findAll(){
        String sql = "SELECT * FROM pedido";
        return jdbcTemplate.query(sql, new PedidoRowMapper());
    }

    // Encontrar pedido por ID;
    public Pedido findById(Long id){
        String sql ="SELECT * FROM pedido WHERE id_pedido = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Pedido.class), id);
    }

    //Deletar pedido por id
    public int deleteById(Long id){
        String sql = "DELETE FROM pedido WHERE Id_pedido = ?";
        return jdbcTemplate.update(sql, id);
    }

    //Criar pedido;
    public int save(Pedido pedido){
        String sql = "INSERT INTO pedido (Id_cliente, Id_artista, Id_arte, Id_serico, descricao, dt_pedido, dt_previsao_entrega, entregue, trabalhando) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, pedido.getId_cliente(), pedido.getId_artista(),pedido.getId_arte(), pedido.getId_servico(), pedido.getDescricao(), pedido.getDt_pedido(), pedido.getDt_previsao_entrega(), pedido.getEntregue(), pedido.getTrabalhando());

    }
}
