package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.dto.LoginResponse;
import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
import com.dossa.ArtisanVault.project.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoginService loginService;

    private Cliente cliente;
    private Artista artista;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        cliente.setSenha("hashCliente");

        artista = new Artista();
        artista.setIdArtista(2L);
        artista.setNome("Artista Teste");
        artista.setEmail("artista@teste.com");
        artista.setSenha("hashArtista");
    }

    @Test
    void login_clienteComCredenciaisValidas_retornaTokenEDadosDoCliente() {
        when(clienteRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("senha123", "hashCliente")).thenReturn(true);
        when(jwtService.generateToken("cliente@teste.com", "CLIENTE", 1L)).thenReturn("token-cliente");

        LoginResponse response = loginService.login("cliente@teste.com", "senha123");

        assertThat(response.getEmail()).isEqualTo("cliente@teste.com");
        assertThat(response.getUserType()).isEqualTo("CLIENTE");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getToken()).isEqualTo("token-cliente");
        verify(artistaRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_clienteComSenhaInvalida_lancaBadCredentialsException() {
        when(clienteRepository.findByEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("errada", "hashCliente")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> loginService.login("cliente@teste.com", "errada"));
        verify(jwtService, never()).generateToken(anyString(), anyString(), any());
    }

    @Test
    void login_artistaComCredenciaisValidas_retornaTokenEDadosDoArtista() {
        when(clienteRepository.findByEmail("artista@teste.com")).thenReturn(Optional.empty());
        when(artistaRepository.findByEmail("artista@teste.com")).thenReturn(Optional.of(artista));
        when(passwordEncoder.matches("senha123", "hashArtista")).thenReturn(true);
        when(jwtService.generateToken("artista@teste.com", "ARTISTA", 2L)).thenReturn("token-artista");

        LoginResponse response = loginService.login("artista@teste.com", "senha123");

        assertThat(response.getUserType()).isEqualTo("ARTISTA");
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getToken()).isEqualTo("token-artista");
    }

    @Test
    void login_artistaComSenhaInvalida_lancaBadCredentialsException() {
        when(clienteRepository.findByEmail("artista@teste.com")).thenReturn(Optional.empty());
        when(artistaRepository.findByEmail("artista@teste.com")).thenReturn(Optional.of(artista));
        when(passwordEncoder.matches("errada", "hashArtista")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> loginService.login("artista@teste.com", "errada"));
    }

    @Test
    void login_emailNaoEncontrado_lancaUsernameNotFoundException() {
        when(clienteRepository.findByEmail("ninguem@teste.com")).thenReturn(Optional.empty());
        when(artistaRepository.findByEmail("ninguem@teste.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> loginService.login("ninguem@teste.com", "qualquer"));
    }
}
