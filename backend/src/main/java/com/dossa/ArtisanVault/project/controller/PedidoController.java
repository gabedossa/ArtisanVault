package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.entity.Pedido;
import com.dossa.ArtisanVault.project.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedido")
public class PedidoController {
    @Autowired
    private PedidoService pedidoService;

    //Listando pedido
    @GetMapping
    public List<Pedido> findAlls(){
        return pedidoService.findAll();
    }

    //Listando pedido por id
    @GetMapping("/{id}")
    public Pedido findById(@PathVariable Long id){
        return pedidoService.findById(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        int result = pedidoService.deleteById(id);

        if (result > 0) {
            return ResponseEntity.ok("pedido excluído com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("pedido não encontrado.");
        }
    }



}
