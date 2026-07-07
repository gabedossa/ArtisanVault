package com.dossa.ArtisanVault.project.controller;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.entity.Pedido;
import com.dossa.ArtisanVault.project.service.ArtistaService;
import com.dossa.ArtisanVault.project.service.ClienteService;
import com.dossa.ArtisanVault.project.service.ImageStorageService;
import com.dossa.ArtisanVault.project.service.PedidoService;
import com.dossa.ArtisanVault.project.service.PortifolioService;
import com.dossa.ArtisanVault.project.service.ServicoService;
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
class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ArtistaService artistaService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private PortifolioService portifolioService;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PedidoController pedidoController;

    private Pedido pedido;
    private Cliente clienteDono;
    private Artista artistaDono;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setId_pedido(1L);
        pedido.setId_cliente(10L);
        pedido.setId_artista(20L);

        clienteDono = new Cliente();
        clienteDono.setIdCliente(10L);

        artistaDono = new Artista();
        artistaDono.setIdArtista(20L);
    }

    @Test
    void findById_clienteDono_podeVer() {
        when(pedidoService.findById(1L)).thenReturn(pedido);
        when(authentication.getName()).thenReturn("cliente@teste.com");
        when(clienteService.findByEmail("cliente@teste.com")).thenReturn(Optional.of(clienteDono));
        when(artistaService.findByEmail("cliente@teste.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = pedidoController.findById(1L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void findById_terceiroNaoRelacionado_retorna403() {
        Cliente outroCliente = new Cliente();
        outroCliente.setIdCliente(99L);

        when(pedidoService.findById(1L)).thenReturn(pedido);
        when(authentication.getName()).thenReturn("terceiro@teste.com");
        when(clienteService.findByEmail("terceiro@teste.com")).thenReturn(Optional.of(outroCliente));
        when(artistaService.findByEmail("terceiro@teste.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = pedidoController.findById(1L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void findById_pedidoInexistente_retorna404() {
        when(pedidoService.findById(1L)).thenReturn(null);

        ResponseEntity<?> response = pedidoController.findById(1L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void meusPedidos_semClienteAutenticado_retorna403() {
        when(authentication.getName()).thenReturn("naoexiste@teste.com");
        when(clienteService.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = pedidoController.meusPedidos(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void pedidosRecebidos_semArtistaAutenticado_retorna403() {
        when(authentication.getName()).thenReturn("naoexiste@teste.com");
        when(artistaService.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = pedidoController.pedidosRecebidos(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteById_artistaDono_podeExcluir() {
        when(pedidoService.findById(1L)).thenReturn(pedido);
        when(authentication.getName()).thenReturn("artista@teste.com");
        when(clienteService.findByEmail("artista@teste.com")).thenReturn(Optional.empty());
        when(artistaService.findByEmail("artista@teste.com")).thenReturn(Optional.of(artistaDono));

        ResponseEntity<String> response = pedidoController.deleteById(1L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteById_naoRelacionado_retorna403() {
        when(pedidoService.findById(1L)).thenReturn(pedido);
        when(authentication.getName()).thenReturn("estranho@teste.com");
        when(clienteService.findByEmail("estranho@teste.com")).thenReturn(Optional.empty());
        when(artistaService.findByEmail("estranho@teste.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response = pedidoController.deleteById(1L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
