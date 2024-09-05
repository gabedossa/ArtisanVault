package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Pedido;
import com.dossa.ArtisanVault.project.entity.Portifolio;
import com.dossa.ArtisanVault.project.service.PortifolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portifolio")
public class PortifolioController {
    @Autowired
    private PortifolioService portServ;

    //Listando pedido
    @GetMapping
    public List<Portifolio> findAlls(){
        return portServ.findAll();
    }

    //Listando pedido por id
    @GetMapping("/{id}")
    public Portifolio findById(@PathVariable Long id){
        return portServ.findById(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        int result = portServ.deleteById(id);

        if (result > 0) {
            return ResponseEntity.ok("portifolio excluído com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("portifolio não encontrado.");
        }
    }
}
