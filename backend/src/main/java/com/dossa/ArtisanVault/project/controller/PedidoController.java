package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.dto.EntregaResponse;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.entity.Pedido;
import com.dossa.ArtisanVault.project.entity.Portifolio;
import com.dossa.ArtisanVault.project.entity.Servico;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import com.dossa.ArtisanVault.project.service.ClienteService;
import com.dossa.ArtisanVault.project.service.ImageStorageService;
import com.dossa.ArtisanVault.project.service.PedidoService;
import com.dossa.ArtisanVault.project.service.PortifolioService;
import com.dossa.ArtisanVault.project.service.ServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedido")
public class PedidoController {
    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private ServicoService servicoService;

    @Autowired
    private PortifolioService portifolioService;

    @Autowired
    private ImageStorageService imageStorageService;

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

    //Cliente solicita um serviço a um artista
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Pedido pedido, Authentication authentication) {
        boolean isCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        if (!isCliente) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas clientes podem solicitar serviços.");
        }

        Optional<Cliente> clienteOpt = clienteService.findByEmail(authentication.getName());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cliente não encontrado.");
        }

        Servico servico = servicoService.findById(pedido.getId_servico());
        if (servico == null) {
            return ResponseEntity.badRequest().body("Serviço não encontrado.");
        }

        pedido.setId_cliente(clienteOpt.get().getIdCliente());
        pedido.setId_artista(servico.getId_artista());
        pedido.setDt_pedido(new Date());
        pedido.setEntregue(false);
        pedido.setTrabalhando(false);

        Pedido criado = pedidoService.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    //Artista entrega a arte de um pedido: cria o trabalho (visível no perfil) e vincula ao pedido/cliente
    @PostMapping(value = "/{id}/entregar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> entregar(
            @PathVariable Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam("imagem") MultipartFile imagem,
            Authentication authentication) {

        Pedido pedido = pedidoService.findById(id);
        if (pedido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pedido não encontrado.");
        }
        if (Boolean.TRUE.equals(pedido.getEntregue())) {
            return ResponseEntity.badRequest().body("Este pedido já foi entregue.");
        }

        Optional<Artista> artistaOpt = artistaService.findByEmail(authentication.getName());
        if (artistaOpt.isEmpty() || !pedido.getId_artista().equals(artistaOpt.get().getIdArtista())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você só pode entregar seus próprios pedidos.");
        }

        try {
            String imagemUrl = imageStorageService.store(imagem);

            Portifolio trabalho = new Portifolio();
            trabalho.setId_artista(pedido.getId_artista());
            trabalho.setId_cliente(pedido.getId_cliente());
            trabalho.setId_pedido(pedido.getId_pedido());
            trabalho.setTitulo(titulo);
            trabalho.setDescricao(descricao);
            trabalho.setImagem_url(imagemUrl);
            Portifolio trabalhoCriado = portifolioService.save(trabalho);

            pedidoService.marcarEntregue(pedido.getId_pedido(), trabalhoCriado.getId_portfolio(), imagemUrl);
            Pedido pedidoAtualizado = pedidoService.findById(id);

            return ResponseEntity.ok(new EntregaResponse(pedidoAtualizado, trabalhoCriado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar imagem.");
        }
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
