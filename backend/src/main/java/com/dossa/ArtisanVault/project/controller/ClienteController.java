package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.service.ClienteService;
import com.dossa.ArtisanVault.project.service.EmailAlreadyInUseException;
import com.dossa.ArtisanVault.project.service.WeakPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/cliente")
public class ClienteController {
    @Autowired
    private ClienteService cliService;

    //Retorna os dados do proprio cliente autenticado
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        Optional<Cliente> cliente = cliService.findByEmail(authentication.getName());
        return cliente.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado."));
    }

    @PostMapping("/post")
    public ResponseEntity<String> createCliente(@RequestBody Cliente cliente){
        try {
            int result = cliService.save(cliente);
            if (result > 0){
                return ResponseEntity.status(HttpStatus.CREATED).body("Cliente criado com sucesso");
            } else{
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar cliente");
            }
        } catch (EmailAlreadyInUseException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        } catch (WeakPasswordException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
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
