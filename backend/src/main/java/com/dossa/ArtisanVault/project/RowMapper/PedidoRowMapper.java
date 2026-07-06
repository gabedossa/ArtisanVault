package com.dossa.ArtisanVault.project.RowMapper;
import com.dossa.ArtisanVault.project.entity.Pedido;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PedidoRowMapper implements RowMapper<Pedido> {
    @Override
    public Pedido mapRow(ResultSet rs, int rowNum) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId_pedido(rs.getLong("id_pedido"));
        pedido.setId_cliente(rs.getLong("id_cliente"));
        pedido.setId_artista(rs.getLong("id_artista"));
        pedido.setId_servico(rs.getLong("id_servico"));
        pedido.setId_arte(rs.getLong("id_arte"));
        pedido.setDescricao(rs.getString("descricao"));
        pedido.setDt_pedido(rs.getDate("dt_pedido"));
        pedido.setDt_previsao_entrega(rs.getDate("dt_previsao_entrega"));
        pedido.setEntregue(rs.getBoolean("entregue"));
        pedido.setTrabalhando(rs.getBoolean("trabalhando"));
        pedido.setId_portfolio(rs.getObject("id_portfolio", Long.class));
        pedido.setImagem_url(rs.getString("imagem_url"));
        return pedido;
    }
}
