package com.dossa.ArtisanVault.project.dto;

import com.dossa.ArtisanVault.project.entity.Portifolio;

public class PortifolioPublicResponse {
    private final Long id_portfolio;
    private final Long id_artista;
    private final String titulo;
    private final String descricao;
    private final String imagem_url;

    public PortifolioPublicResponse(Portifolio portifolio) {
        this.id_portfolio = portifolio.getId_portfolio();
        this.id_artista = portifolio.getId_artista();
        this.titulo = portifolio.getTitulo();
        this.descricao = portifolio.getDescricao();
        this.imagem_url = portifolio.getImagem_url();
    }

    public Long getId_portfolio() {
        return id_portfolio;
    }

    public Long getId_artista() {
        return id_artista;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getImagem_url() {
        return imagem_url;
    }
}
