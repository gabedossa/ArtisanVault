package com.dossa.ArtisanVault.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "servico")
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico")
    private Long id_servico;
    @Column(name = "id_artista")
    private Long id_artista;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "valor_servico")
    private Double valor_servico;

    public Long getId_servico() {
        return id_servico;
    }

    public Long getId_artista() {
        return id_artista;
    }


    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor_servico() {
        return valor_servico;
    }

    public void setValor_servico(Double valor_servico) {
        this.valor_servico = valor_servico;
    }
}
