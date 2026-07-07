package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import com.dossa.ArtisanVault.project.repository.EmailRegistroRepository;
import com.dossa.ArtisanVault.project.util.EmailNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArtistaService {

    private static final int MIN_SENHA_LENGTH = 6;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private EmailRegistroRepository emailRegistroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Método para encontrar todos os artistas
    public List<Artista> findAll() {
        return artistaRepository.findAll();
    }

    // Método para encontrar um artista por ID
    public Artista findById(Long id) {
        return artistaRepository.findById(id);
    }

    // Método para criar um novo artista
    @Transactional
    public int save(Artista artista) {
        String email = EmailNormalizer.normalize(artista.getEmail());
        validateSenha(artista.getSenha());
        if (!emailRegistroRepository.tryReserve(email)) {
            throw new EmailAlreadyInUseException(email);
        }
        artista.setEmail(email);
        artista.setSenha(passwordEncoder.encode(artista.getSenha()));
        return artistaRepository.save(artista);
    }

    // Método para atualizar um artista existente
    @Transactional
    public int update(Artista artista) {
        Artista existing = artistaRepository.findById(artista.getIdArtista());
        String email = EmailNormalizer.normalize(artista.getEmail());
        String existingEmail = EmailNormalizer.normalize(existing.getEmail());
        if (!email.equals(existingEmail)) {
            if (!emailRegistroRepository.tryReserve(email)) {
                throw new EmailAlreadyInUseException(email);
            }
            emailRegistroRepository.release(existingEmail);
        }
        artista.setEmail(email);

        if (artista.getSenha() == null || artista.getSenha().isBlank()) {
            artista.setSenha(existing.getSenha());
        } else {
            validateSenha(artista.getSenha());
            artista.setSenha(passwordEncoder.encode(artista.getSenha()));
        }
        return artistaRepository.update(artista);
    }

    // Método para excluir um artista por ID
    @Transactional
    public int deleteById(Long id) {
        Artista existing = artistaRepository.findById(id);
        int result = artistaRepository.deleteById(id);
        if (result > 0) {
            emailRegistroRepository.release(EmailNormalizer.normalize(existing.getEmail()));
        }
        return result;
    }

    // Método para encontrar um artista por email
    public Optional<Artista> findByEmail(String email) {
        return artistaRepository.findByEmail(EmailNormalizer.normalize(email));
    }

    private void validateSenha(String senha) {
        if (senha == null || senha.length() < MIN_SENHA_LENGTH) {
            throw new WeakPasswordException();
        }
    }
}
