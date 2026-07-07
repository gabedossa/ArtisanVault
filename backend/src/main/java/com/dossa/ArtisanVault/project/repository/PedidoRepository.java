package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.RowMapper.PedidoRowMapper;
import com.dossa.ArtisanVault.project.entity.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
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
        String sql = "SELECT * FROM pedido WHERE id_pedido = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new PedidoRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    //Encontrar pedidos de um cliente
    public List<Pedido> findByCliente(Long idCliente){
        String sql = "SELECT * FROM pedido WHERE id_cliente = ?";
        return jdbcTemplate.query(sql, new PedidoRowMapper(), idCliente);
    }

    //Encontrar pedidos recebidos por um artista
    public List<Pedido> findByArtista(Long idArtista){
        String sql = "SELECT * FROM pedido WHERE id_artista = ?";
        return jdbcTemplate.query(sql, new PedidoRowMapper(), idArtista);
    }

    //Deletar pedido por id
    public int deleteById(Long id){
        String sql = "DELETE FROM pedido WHERE id_pedido = ?";
        return jdbcTemplate.update(sql, id);
    }

    //Criar pedido;
    public Pedido save(Pedido pedido){
        String sql = "INSERT INTO pedido (id_cliente, id_artista, id_servico, descricao, dt_pedido, dt_previsao_entrega, entregue, trabalhando) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id_pedido"});
            ps.setLong(1, pedido.getId_cliente());
            ps.setLong(2, pedido.getId_artista());
            ps.setLong(3, pedido.getId_servico());
            ps.setString(4, pedido.getDescricao());
            ps.setDate(5, pedido.getDt_pedido() != null ? new java.sql.Date(pedido.getDt_pedido().getTime()) : null);
            ps.setDate(6, pedido.getDt_previsao_entrega() != null ? new java.sql.Date(pedido.getDt_previsao_entrega().getTime()) : null);
            ps.setBoolean(7, Boolean.TRUE.equals(pedido.getEntregue()));
            ps.setBoolean(8, Boolean.TRUE.equals(pedido.getTrabalhando()));
            return ps;
        }, keyHolder);
        pedido.setId_pedido(keyHolder.getKeyAs(Number.class).longValue());
        return pedido;
    }

    //Marcar pedido como entregue, vinculando o trabalho criado
    public int marcarEntregue(Long idPedido, Long idPortfolio, String imagemUrl){
        String sql = "UPDATE pedido SET entregue = true, trabalhando = false, id_portfolio = ?, imagem_url = ? WHERE id_pedido = ?";
        return jdbcTemplate.update(sql, idPortfolio, imagemUrl, idPedido);
    }

    //Marcar pedido como em andamento
    public int marcarTrabalhando(Long idPedido){
        String sql = "UPDATE pedido SET trabalhando = true WHERE id_pedido = ?";
        return jdbcTemplate.update(sql, idPedido);
    }
}
