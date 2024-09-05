package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Portifolio;
import com.dossa.ArtisanVault.project.entity.Servico;
import com.dossa.ArtisanVault.project.service.PortifolioService;
import com.dossa.ArtisanVault.project.service.ServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servico")
public class ServicoController {
    @Autowired
    private ServicoService servicoServ;

    //Listando pedido
    @GetMapping
    public List<Servico> findAll(){
        return servicoServ.findAll();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        int result = servicoServ.deleteById(id);

        if (result > 0) {
            return ResponseEntity.ok("portifolio excluído com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("portifolio não encontrado.");
        }
    }
}
