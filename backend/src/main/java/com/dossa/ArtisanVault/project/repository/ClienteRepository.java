package com.dossa.ArtisanVault.project.repository;

import com.dossa.ArtisanVault.project.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
