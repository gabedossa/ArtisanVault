package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Arte;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.service.ArteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/arte")
public class ArteController {
    @Autowired
    private ArteService artService;

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
    public ResponseEntity<String> createArte(@RequestBody Arte arte){
        int result= artService.save(arte);
        if (result > 0){
            return ResponseEntity.status(HttpStatus.CREATED).body("Arte criado com sucesso");
        } else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar arte");
        }


    }
    // Deletando Arte
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteArte(@PathVariable Long id) {
        int result = artService.deleteById(id);

        if (result > 0) {
            return ResponseEntity.ok("Arte excluído com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arte não encontrado.");
        }
    }
}
