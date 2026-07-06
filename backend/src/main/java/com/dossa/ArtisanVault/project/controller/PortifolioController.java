package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Portifolio;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import com.dossa.ArtisanVault.project.service.ImageStorageService;
import com.dossa.ArtisanVault.project.service.PortifolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/portifolio")
public class PortifolioController {
    @Autowired
    private PortifolioService portServ;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private ImageStorageService imageStorageService;

    //Listando portfólios (trabalhos)
    @GetMapping
    public List<Portifolio> findAlls(){
        return portServ.findAll();
    }

    //Listando portfólio por id
    @GetMapping("/{id}")
    public Portifolio findById(@PathVariable Long id){
        return portServ.findById(id);
    }

    //Criando um novo trabalho (imagem + título + descrição)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam("imagem") MultipartFile imagem,
            Authentication authentication) {

        boolean isArtista = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ARTISTA"));
        if (!isArtista) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas artistas podem publicar trabalhos.");
        }

        Optional<Artista> artistaOpt = artistaService.findByEmail(authentication.getName());
        if (artistaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Artista não encontrado.");
        }

        try {
            String imagemUrl = imageStorageService.store(imagem);

            Portifolio portifolio = new Portifolio();
            portifolio.setId_artista(artistaOpt.get().getIdArtista());
            portifolio.setTitulo(titulo);
            portifolio.setDescricao(descricao);
            portifolio.setImagem_url(imagemUrl);

            Portifolio criado = portServ.save(portifolio);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao salvar imagem.");
        }
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
