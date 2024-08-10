package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/artistas")
public class ArtistaController {
    @Autowired
    private ArtistaService artService;
    //Listando Artista
    @GetMapping
    public List<Artista> getAllArtistas(){
        return artService.findAll();
    }

    //Listando Artista por id
    @GetMapping("/{id}")
    public Artista findById(@PathVariable Long id){
    return artService.findById(id);
    }

    //Criando Artista
    @PostMapping("/post")
    public ResponseEntity<String> createArtista(@RequestBody Artista artista){
        int result= artService.save(artista);
        if (result > 0){
            return ResponseEntity.status(HttpStatus.CREATED).body("Artista criado com sucesso");
        } else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar artista");
        }
    }
    // Deletando Artista
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteArtista(@PathVariable Long id) {
        int result = artService.deleteById(id);

        if (result > 0) {
            return ResponseEntity.ok("Artista excluído com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artista não encontrado.");
        }
    }

    //Atualizando artista
    @PutMapping("/{id}")
    public ResponseEntity<String> updateArtista(@PathVariable Long id, @RequestBody Artista artista) {
        artista.setIdArtista(id);
        int result = artService.update(artista);
        if (result > 0) {
            return ResponseEntity.ok("Artista atualizado com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artista não encontrado.");
        }
    }
}
