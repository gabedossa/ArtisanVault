package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Pedido;
import com.dossa.ArtisanVault.project.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepo;

    public List<Pedido> findAll(){return pedidoRepo.findAll();}

    public Pedido findById(Long id){
        return pedidoRepo.findById(id);
    }

    public List<Pedido> findByCliente(Long idCliente){
        return pedidoRepo.findByCliente(idCliente);
    }

    public List<Pedido> findByArtista(Long idArtista){
        return pedidoRepo.findByArtista(idArtista);
    }

    public Pedido save(Pedido pedido){
        return pedidoRepo.save(pedido);
    }

    public int marcarEntregue(Long idPedido, Long idPortfolio, String imagemUrl){
        return pedidoRepo.marcarEntregue(idPedido, idPortfolio, imagemUrl);
    }

    public int marcarTrabalhando(Long idPedido){
        return pedidoRepo.marcarTrabalhando(idPedido);
    }

    public int deleteById(Long id){
        return pedidoRepo.deleteById(id);
    }
}
