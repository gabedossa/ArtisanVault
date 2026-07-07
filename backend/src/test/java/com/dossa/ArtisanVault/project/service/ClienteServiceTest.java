package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Cliente;
import com.dossa.ArtisanVault.project.repository.ClienteRepository;
import com.dossa.ArtisanVault.project.repository.EmailRegistroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepo;

    @Mock
    private EmailRegistroRepository emailRegistroRepository;

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
        when(emailRegistroRepository.tryReserve("cliente@teste.com")).thenReturn(true);
        when(passwordEncoder.encode("senhaEmTexto")).thenReturn("senhaCriptografada");
        when(clienteRepo.save(cliente)).thenReturn(1);

        int linhasAfetadas = clienteService.save(cliente);

        assertThat(cliente.getSenha()).isEqualTo("senhaCriptografada");
        assertThat(linhasAfetadas).isEqualTo(1);
    }

    @Test
    void save_normalizaEmailAntesDeSalvar() {
        cliente.setEmail("  Cliente@Teste.com  ");
        when(emailRegistroRepository.tryReserve("cliente@teste.com")).thenReturn(true);
        when(clienteRepo.save(cliente)).thenReturn(1);

        clienteService.save(cliente);

        assertThat(cliente.getEmail()).isEqualTo("cliente@teste.com");
    }

    @Test
    void save_emailJaReservado_lancaExcecao() {
        when(emailRegistroRepository.tryReserve("cliente@teste.com")).thenReturn(false);

        assertThatThrownBy(() -> clienteService.save(cliente))
                .isInstanceOf(EmailAlreadyInUseException.class);
        verify(clienteRepo, never()).save(any());
    }

    @Test
    void save_senhaVazia_lancaExcecaoENaoReservaEmail() {
        cliente.setSenha("");

        assertThatThrownBy(() -> clienteService.save(cliente))
                .isInstanceOf(WeakPasswordException.class);
        verify(emailRegistroRepository, never()).tryReserve(any());
        verify(clienteRepo, never()).save(any());
    }

    @Test
    void update_comSenhaNula_mantemSenhaExistente() {
        cliente.setSenha(null);
        Cliente existente = new Cliente();
        existente.setIdCliente(1L);
        existente.setEmail("cliente@teste.com");
        existente.setSenha("senhaAntigaCriptografada");
        when(clienteRepo.findById(1L)).thenReturn(existente);
        when(clienteRepo.update(cliente)).thenReturn(1);

        clienteService.update(cliente);

        assertThat(cliente.getSenha()).isEqualTo("senhaAntigaCriptografada");
        verify(passwordEncoder, never()).encode(anyString());
        verify(emailRegistroRepository, never()).tryReserve(any());
    }

    @Test
    void update_paraEmailJaReservado_lancaExcecao() {
        Cliente existente = new Cliente();
        existente.setIdCliente(1L);
        existente.setEmail("outro@teste.com");
        when(clienteRepo.findById(1L)).thenReturn(existente);
        when(emailRegistroRepository.tryReserve("cliente@teste.com")).thenReturn(false);

        assertThatThrownBy(() -> clienteService.update(cliente))
                .isInstanceOf(EmailAlreadyInUseException.class);
        verify(clienteRepo, never()).update(any());
        verify(emailRegistroRepository, never()).release(any());
    }

    @Test
    void update_paraNovoEmailLivre_liberaEmailAntigo() {
        Cliente existente = new Cliente();
        existente.setIdCliente(1L);
        existente.setEmail("outro@teste.com");
        cliente.setSenha(null);
        when(clienteRepo.findById(1L)).thenReturn(existente);
        when(emailRegistroRepository.tryReserve("cliente@teste.com")).thenReturn(true);
        when(clienteRepo.update(cliente)).thenReturn(1);

        clienteService.update(cliente);

        verify(emailRegistroRepository).release("outro@teste.com");
    }

    @Test
    void deleteById_liberaEmailNoRegistro() {
        when(clienteRepo.findById(1L)).thenReturn(cliente);
        when(clienteRepo.deleteById(1L)).thenReturn(1);

        int linhasAfetadas = clienteService.deleteById(1L);

        assertThat(linhasAfetadas).isEqualTo(1);
        verify(clienteRepo).deleteById(1L);
        verify(emailRegistroRepository).release("cliente@teste.com");
    }
}
