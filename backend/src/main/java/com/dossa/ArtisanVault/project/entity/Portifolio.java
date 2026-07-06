package com.dossa.ArtisanVault.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "portfolio")
public class Portifolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_portfolio")
    private Long id_portfolio;
    @Column(name = "id_artista")
    private Long id_artista;
    @Column(name = "titulo")
    private String titulo;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "imagem_url")
    private String imagem_url;
    @Column(name = "id_cliente")
    private Long id_cliente;
    @Column(name = "id_pedido")
    private Long id_pedido;

    public void setId_portfolio(Long id_portfolio) {
        this.id_portfolio = id_portfolio;
    }

    public void setId_artista(Long id_artista) {
        this.id_artista = id_artista;
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

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagem_url() {
        return imagem_url;
    }

    public void setImagem_url(String imagem_url) {
        this.imagem_url = imagem_url;
    }

    public Long getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(Long id_cliente) {
        this.id_cliente = id_cliente;
    }

    public Long getId_pedido() {
        return id_pedido;
    }

    public void setId_pedido(Long id_pedido) {
        this.id_pedido = id_pedido;
    }
}
