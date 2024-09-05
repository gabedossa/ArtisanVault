package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.LoginArtista;
import com.dossa.ArtisanVault.project.entity.LoginArtistaResponse;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/artistas")
public class ArtistaController {
    @Autowired
    private ArtistaRepository artistaRepository;

    // Método para encontrar todos os artistas
    @GetMapping
    public ResponseEntity<List<Artista>> findAll() {
        List<Artista> artistas = artistaRepository.findAll();
        return ResponseEntity.ok(artistas);
    }

    // Método para encontrar um artista por ID
    @GetMapping("/{id}")
    public ResponseEntity<Artista> findById(@PathVariable Long id) {
        Artista artista = artistaRepository.findById(id);
        if (artista != null) {
            return ResponseEntity.ok(artista);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Método para login de artista
    @PostMapping("/login")
    public ResponseEntity<String> verificaArtista(@RequestParam String email, @RequestParam String senha) {
        Optional<Artista> artistaOpt = artistaRepository.LoginArtista(email, senha);
        if (artistaOpt.isPresent()) {
            return ResponseEntity.ok("Login bem-sucedido");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E-mail ou senha inválidos");
        }
    }

    // Método para criar um novo artista
    @PostMapping
    public ResponseEntity<String> save(@RequestBody Artista artista) {
        int result = artistaRepository.save(artista);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Artista criado com sucesso");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar artista");
        }
    }

    // Método para atualizar um artista existente
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody Artista artista) {
        artista.setIdArtista(id);
        int result = artistaRepository.update(artista);
        if (result > 0) {
            return ResponseEntity.ok("Artista atualizado com sucesso");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artista não encontrado");
        }
    }

    // Método para excluir um artista por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        int result = artistaRepository.deleteById(id);
        if (result > 0) {
            return ResponseEntity.ok("Artista excluído com sucesso");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artista não encontrado");
        }
    }

    // Método para encontrar um artista por email
    @GetMapping("/email")
    public ResponseEntity<Optional<Artista>> findByEmail(@RequestParam String email) {
        Optional<Artista> artista = artistaRepository.findByEmail(email);
        if (artista.isPresent()) {
            return ResponseEntity.ok(artista);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty());
        }
    }

}
