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
}
