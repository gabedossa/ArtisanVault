package com.dossa.ArtisanVault.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "arte")
public class Arte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_arte")
    private Long id_arte;
    @Column(name = "id_portfolio")
    private Long id_portfolio;
    @Column(name = "titulo")
    private String titulo;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "vote")
    private Long vote;

    public Long getId_arte() {
        return id_arte;
    }

    public Long getId_portfolio() {
        return id_portfolio;
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

    public Long getVote() {
        return vote;
    }

    public void setVote(Long vote) {
        this.vote = vote;
    }
}
