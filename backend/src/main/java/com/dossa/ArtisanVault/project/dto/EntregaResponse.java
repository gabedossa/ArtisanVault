package com.dossa.ArtisanVault.project.dto;

import com.dossa.ArtisanVault.project.entity.Pedido;
import com.dossa.ArtisanVault.project.entity.Portifolio;

public class EntregaResponse {
    private Pedido pedido;
    private Portifolio trabalho;

    public EntregaResponse(Pedido pedido, Portifolio trabalho) {
        this.pedido = pedido;
        this.trabalho = trabalho;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Portifolio getTrabalho() {
        return trabalho;
    }
}
