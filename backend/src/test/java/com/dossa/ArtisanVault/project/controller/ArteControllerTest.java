package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Arte;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Portifolio;
import com.dossa.ArtisanVault.project.service.ArteService;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import com.dossa.ArtisanVault.project.service.PortifolioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArteControllerTest {

    @Mock
    private ArteService artService;

    @Mock
    private PortifolioService portifolioService;

    @Mock
    private ArtistaService artistaService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ArteController arteController;

    private Portifolio portifolioDoDono;
    private Artista dono;

    @BeforeEach
    void setUp() {
        dono = new Artista();
        dono.setIdArtista(1L);
        dono.setEmail("dono@teste.com");

        portifolioDoDono = new Portifolio();
        portifolioDoDono.setId_portfolio(10L);
        portifolioDoDono.setId_artista(1L);
    }

    @Test
    void createArte_donoDoPortfolio_criaComSucesso() {
        Arte arte = new Arte();
        arte.setId_portfolio(10L);
        when(portifolioService.findById(10L)).thenReturn(portifolioDoDono);
        when(authentication.getName()).thenReturn("dono@teste.com");
        when(artistaService.findByEmail("dono@teste.com")).thenReturn(Optional.of(dono));
        when(artService.save(arte)).thenReturn(1);

        ResponseEntity<String> response = arteController.createArte(arte, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createArte_naoDonoDoPortfolio_retorna403() {
        Artista outroArtista = new Artista();
        outroArtista.setIdArtista(2L);
        outroArtista.setEmail("invasor@teste.com");

        Arte arte = new Arte();
        arte.setId_portfolio(10L);
        when(portifolioService.findById(10L)).thenReturn(portifolioDoDono);
        when(authentication.getName()).thenReturn("invasor@teste.com");
        when(artistaService.findByEmail("invasor@teste.com")).thenReturn(Optional.of(outroArtista));

        ResponseEntity<String> response = arteController.createArte(arte, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createArte_portfolioInexistente_retorna404() {
        Arte arte = new Arte();
        arte.setId_portfolio(999L);
        when(portifolioService.findById(999L)).thenReturn(null);

        ResponseEntity<String> response = arteController.createArte(arte, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteArte_naoDonoDoPortfolio_retorna403() {
        Artista outroArtista = new Artista();
        outroArtista.setIdArtista(2L);

        Arte existente = new Arte();
        existente.setId_arte(5L);
        existente.setId_portfolio(10L);

        when(artService.findById(5L)).thenReturn(existente);
        when(portifolioService.findById(10L)).thenReturn(portifolioDoDono);
        when(authentication.getName()).thenReturn("invasor@teste.com");
        when(artistaService.findByEmail("invasor@teste.com")).thenReturn(Optional.of(outroArtista));

        ResponseEntity<String> response = arteController.deleteArte(5L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteArte_donoDoPortfolio_excluiComSucesso() {
        Arte existente = new Arte();
        existente.setId_arte(5L);
        existente.setId_portfolio(10L);

        when(artService.findById(5L)).thenReturn(existente);
        when(portifolioService.findById(10L)).thenReturn(portifolioDoDono);
        when(authentication.getName()).thenReturn("dono@teste.com");
        when(artistaService.findByEmail("dono@teste.com")).thenReturn(Optional.of(dono));
        when(artService.deleteById(5L)).thenReturn(1);

        ResponseEntity<String> response = arteController.deleteArte(5L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
