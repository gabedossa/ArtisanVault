package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Servico;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import com.dossa.ArtisanVault.project.service.ServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/servico")
public class ServicoController {
    @Autowired
    private ServicoService servicoServ;

    @Autowired
    private ArtistaService artistaService;

    //Listando serviços
    @GetMapping
    public List<Servico> findAll(){
        return servicoServ.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servico> findById(@PathVariable Long id) {
        Servico servico = servicoServ.findById(id);
        if (servico == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(servico);
    }

    //Criando um novo serviço (dono = artista autenticado)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Servico servico, Authentication authentication) {
        Artista artista = autenticarArtista(authentication);
        if (artista == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas artistas podem criar serviços.");
        }

        servico.setId_artista(artista.getIdArtista());
        Servico criado = servicoServ.save(servico);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    //Atualizando um serviço (somente o dono)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Servico servico, Authentication authentication) {
        Servico existente = servicoServ.findById(id);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Serviço não encontrado.");
        }

        Artista artista = autenticarArtista(authentication);
        if (artista == null || !existente.getId_artista().equals(artista.getIdArtista())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você só pode editar seus próprios serviços.");
        }

        servico.setId_servico(id);
        servico.setId_artista(existente.getId_artista());
        servicoServ.update(servico);
        return ResponseEntity.ok(servico);
    }

    //Excluindo um serviço (somente o dono)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id, Authentication authentication) {
        Servico existente = servicoServ.findById(id);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Serviço não encontrado.");
        }

        Artista artista = autenticarArtista(authentication);
        if (artista == null || !existente.getId_artista().equals(artista.getIdArtista())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você só pode remover seus próprios serviços.");
        }

        servicoServ.deleteById(id);
        return ResponseEntity.ok("Serviço excluído com sucesso.");
    }

    private Artista autenticarArtista(Authentication authentication) {
        boolean isArtista = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ARTISTA"));
        if (!isArtista) {
            return null;
        }
        Optional<Artista> artistaOpt = artistaService.findByEmail(authentication.getName());
        return artistaOpt.orElse(null);
    }
}
