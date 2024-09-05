package com.dossa.ArtisanVault.project.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "pedido")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_pedido;
    @Column(name = "id_cliente")
    private Long id_cliente;
    @Column(name = "id_artista")
    private Long id_artista;
    @Column(name = "id_servico")
    private Long id_servico;
    @Column(name = "id_arte")
    private Long id_arte;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "dt_pedido")
    private Date dt_pedido;
    @Column(name = "dt_previsao_entrega")
    private Date dt_previsao_entrega;
    @Column(name = "entregue")
    private Boolean entregue;
    @Column(name = "trabalhando")
    private Boolean trabalhando;

    public void setId_pedido(Long id_pedido) {
        this.id_pedido = id_pedido;
    }

    public void setId_cliente(Long id_cliente) {
        this.id_cliente = id_cliente;
    }

    public void setId_artista(Long id_artista) {
        this.id_artista = id_artista;
    }

    public void setId_servico(Long id_servico) {
        this.id_servico = id_servico;
    }

    public void setId_arte(Long id_arte) {
        this.id_arte = id_arte;
    }

    public Long getId_pedido() {
        return id_pedido;
    }


    public Long getId_cliente() {
        return id_cliente;
    }

    public Long getId_artista() {
        return id_artista;
    }

    public Long getId_servico() {
        return id_servico;
    }

    public Long getId_arte() {
        return id_arte;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Date getDt_pedido() {
        return dt_pedido;
    }

    public void setDt_pedido(Date dt_pedido) {
        this.dt_pedido = dt_pedido;
    }

    public Date getDt_previsao_entrega() {
        return dt_previsao_entrega;
    }

    public void setDt_previsao_entrega(Date dt_previsao_entrega) {
        this.dt_previsao_entrega = dt_previsao_entrega;
    }

    public Boolean getEntregue() {
        return entregue;
    }

    public void setEntregue(Boolean entregue) {
        this.entregue = entregue;
    }

    public Boolean getTrabalhando() {
        return trabalhando;
    }

    public void setTrabalhando(Boolean trabalhando) {
        this.trabalhando = trabalhando;
    }
}
