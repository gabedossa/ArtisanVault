package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cliente")
public class ClienteController {
    @Autowired
    private ClienteService cliService;

    //Listando Artista
    @GetMapping
    public List<Cliente> getAllArtistas(){
        return cliService.findAll();
    }

    //Listando Artista por id
    @GetMapping("/{id}")
    public Cliente findById(@PathVariable Long id){
        return cliService.findById(id);
    }

    @PostMapping("/post")
    public ResponseEntity<String> createCliente(@RequestBody Cliente cliente){
        int result= cliService.save(cliente);
        if (result > 0){
            return ResponseEntity.status(HttpStatus.CREATED).body("Cliente criado com sucesso");
        } else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar cliente");
        }
    }
    // Deletando Artista
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCliente(@PathVariable Long id, Authentication authentication) {
        Optional<Cliente> autenticado = cliService.findByEmail(authentication.getName());
        if (autenticado.isEmpty() || !autenticado.get().getIdCliente().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você só pode remover sua própria conta.");
        }

        int result = cliService.deleteById(id);

        if (result > 0) {
            return ResponseEntity.ok("Artista excluído com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artista não encontrado.");
        }
    }
}
