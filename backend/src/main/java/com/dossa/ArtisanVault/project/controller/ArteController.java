package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Arte;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Portifolio;
import com.dossa.ArtisanVault.project.service.ArteService;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import com.dossa.ArtisanVault.project.service.PortifolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/arte")
public class ArteController {
    @Autowired
    private ArteService artService;

    @Autowired
    private PortifolioService portifolioService;

    @Autowired
    private ArtistaService artistaService;

    //Listando Artista
    @GetMapping
    public List<Arte> getAllArte(){
        return artService.findAll();
    }

    //Listando Arte por id
    @GetMapping("/{id}")
    public Arte findById(@PathVariable Long id){
        return artService.findById(id);
    }

    //Criando Arte
    @PostMapping("/post")
    public ResponseEntity<String> createArte(@RequestBody Arte arte, Authentication authentication){
        Portifolio portifolio = portifolioService.findById(arte.getId_portfolio());
        if (portifolio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Portfolio não encontrado.");
        }

        Optional<Artista> artistaOpt = artistaService.findByEmail(authentication.getName());
        if (artistaOpt.isEmpty() || !portifolio.getId_artista().equals(artistaOpt.get().getIdArtista())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você só pode criar artes nos seus próprios portfolios.");
        }

        int result= artService.save(arte);
        if (result > 0){
            return ResponseEntity.status(HttpStatus.CREATED).body("Arte criado com sucesso");
        } else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar arte");
        }
    }
    // Deletando Arte
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteArte(@PathVariable Long id, Authentication authentication) {
        Arte existente = artService.findById(id);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arte não encontrado.");
        }

        Portifolio portifolio = portifolioService.findById(existente.getId_portfolio());
        Optional<Artista> artistaOpt = artistaService.findByEmail(authentication.getName());
        if (portifolio == null || artistaOpt.isEmpty() || !portifolio.getId_artista().equals(artistaOpt.get().getIdArtista())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você só pode remover artes dos seus próprios trabalhos.");
        }

        int result = artService.deleteById(id);
        if (result > 0) {
            return ResponseEntity.ok("Arte excluído com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arte não encontrado.");
        }
    }
}
