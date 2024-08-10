package com.dossa.ArtisanVault.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "servico")
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico")
    private Long id_servico;
    private
}
