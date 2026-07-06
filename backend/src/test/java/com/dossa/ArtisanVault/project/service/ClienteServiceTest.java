package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        cliente.setSenha("senhaEmTexto");
    }

    @Test
    void save_criptografaSenhaAntesDeSalvar() {
        when(passwordEncoder.encode("senhaEmTexto")).thenReturn("senhaCriptografada");
        when(clienteRepo.save(cliente)).thenReturn(1);

        int linhasAfetadas = clienteService.save(cliente);

        assertThat(cliente.getSenha()).isEqualTo("senhaCriptografada");
        assertThat(linhasAfetadas).isEqualTo(1);
    }

    @Test
    void update_comSenhaNula_mantemSenhaExistente() {
        cliente.setSenha(null);
        Cliente existente = new Cliente();
        existente.setIdCliente(1L);
        existente.setSenha("senhaAntigaCriptografada");
        when(clienteRepo.findById(1L)).thenReturn(existente);
        when(clienteRepo.update(cliente)).thenReturn(1);

        clienteService.update(cliente);

        assertThat(cliente.getSenha()).isEqualTo("senhaAntigaCriptografada");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteById_delegaParaRepository() {
        when(clienteRepo.deleteById(1L)).thenReturn(1);

        int linhasAfetadas = clienteService.deleteById(1L);

        assertThat(linhasAfetadas).isEqualTo(1);
        verify(clienteRepo).deleteById(1L);
    }
}
