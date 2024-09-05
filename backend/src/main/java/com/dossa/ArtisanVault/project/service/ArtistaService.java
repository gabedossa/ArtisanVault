package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
public class ArtistaService {

    @Autowired
    private ArtistaRepository artistaRepository;

    // Método para encontrar todos os artistas
    public List<Artista> findAll() {
        return artistaRepository.findAll();
    }

    // Método para encontrar um artista por ID
    public Artista findById(Long id) {
        return artistaRepository.findById(id);
    }

    // Método para login de artista (renomeado para verificaArtista)
    public Optional<Artista> verificaArtista(String email, String senha) {
        return artistaRepository.LoginArtista(email, senha);
    }

    // Método para criar um novo artista
    public int save(Artista artista) {
        return artistaRepository.save(artista);
    }

    // Método para atualizar um artista existente
    public int update(Artista artista) {
        return artistaRepository.update(artista);
    }

    // Método para excluir um artista por ID
    public int deleteById(Long id) {
        return artistaRepository.deleteById(id);
    }

    // Método para encontrar um artista por email
    public Optional<Artista> findByEmail(String email) {
        return artistaRepository.findByEmail(email);
    }
}
