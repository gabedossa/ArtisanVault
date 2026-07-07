package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
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
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private EmailRegistroRepository emailRegistroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ArtistaService artistaService;

    private Artista artista;

    @BeforeEach
    void setUp() {
        artista = new Artista();
        artista.setIdArtista(1L);
        artista.setNome("Artista Teste");
        artista.setEmail("artista@teste.com");
        artista.setSenha("senhaEmTexto");
    }

    @Test
    void save_criptografaSenhaAntesDeSalvar() {
        when(emailRegistroRepository.tryReserve("artista@teste.com")).thenReturn(true);
        when(passwordEncoder.encode("senhaEmTexto")).thenReturn("senhaCriptografada");
        when(artistaRepository.save(artista)).thenReturn(1);

        int linhasAfetadas = artistaService.save(artista);

        assertThat(artista.getSenha()).isEqualTo("senhaCriptografada");
        assertThat(linhasAfetadas).isEqualTo(1);
        verify(artistaRepository).save(artista);
    }

    @Test
    void save_normalizaEmailAntesDeSalvar() {
        artista.setEmail("  Artista@Teste.com  ");
        when(emailRegistroRepository.tryReserve("artista@teste.com")).thenReturn(true);
        when(artistaRepository.save(artista)).thenReturn(1);

        artistaService.save(artista);

        assertThat(artista.getEmail()).isEqualTo("artista@teste.com");
    }

    @Test
    void save_emailJaReservado_lancaExcecao() {
        when(emailRegistroRepository.tryReserve("artista@teste.com")).thenReturn(false);

        assertThatThrownBy(() -> artistaService.save(artista))
                .isInstanceOf(EmailAlreadyInUseException.class);
        verify(artistaRepository, never()).save(any());
    }

    @Test
    void save_senhaCurta_lancaExcecaoENaoReservaEmail() {
        artista.setSenha("123");

        assertThatThrownBy(() -> artistaService.save(artista))
                .isInstanceOf(WeakPasswordException.class);
        verify(emailRegistroRepository, never()).tryReserve(any());
        verify(artistaRepository, never()).save(any());
    }

    @Test
    void update_comSenhaEmBranco_mantemSenhaExistente() {
        artista.setSenha("");
        Artista existente = new Artista();
        existente.setIdArtista(1L);
        existente.setEmail("artista@teste.com");
        existente.setSenha("senhaAntigaCriptografada");
        when(artistaRepository.findById(1L)).thenReturn(existente);
        when(artistaRepository.update(artista)).thenReturn(1);

        artistaService.update(artista);

        assertThat(artista.getSenha()).isEqualTo("senhaAntigaCriptografada");
        verify(passwordEncoder, never()).encode(anyString());
        verify(emailRegistroRepository, never()).tryReserve(any());
    }

    @Test
    void update_comNovaSenha_criptografaNovaSenha() {
        artista.setSenha("novaSenha");
        Artista existente = new Artista();
        existente.setIdArtista(1L);
        existente.setEmail("artista@teste.com");
        existente.setSenha("senhaAntigaCriptografada");
        when(artistaRepository.findById(1L)).thenReturn(existente);
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaSenhaCriptografada");
        when(artistaRepository.update(artista)).thenReturn(1);

        artistaService.update(artista);

        assertThat(artista.getSenha()).isEqualTo("novaSenhaCriptografada");
        verify(artistaRepository).findById(1L);
    }

    @Test
    void update_comNovaSenhaCurta_lancaExcecao() {
        artista.setSenha("123");
        Artista existente = new Artista();
        existente.setIdArtista(1L);
        existente.setEmail("artista@teste.com");
        existente.setSenha("senhaAntigaCriptografada");
        when(artistaRepository.findById(1L)).thenReturn(existente);

        assertThatThrownBy(() -> artistaService.update(artista))
                .isInstanceOf(WeakPasswordException.class);
        verify(artistaRepository, never()).update(any());
    }

    @Test
    void update_paraEmailJaReservado_lancaExcecao() {
        Artista existente = new Artista();
        existente.setIdArtista(1L);
        existente.setEmail("outro@teste.com");
        when(artistaRepository.findById(1L)).thenReturn(existente);
        when(emailRegistroRepository.tryReserve("artista@teste.com")).thenReturn(false);

        assertThatThrownBy(() -> artistaService.update(artista))
                .isInstanceOf(EmailAlreadyInUseException.class);
        verify(artistaRepository, never()).update(any());
        verify(emailRegistroRepository, never()).release(any());
    }

    @Test
    void update_paraNovoEmailLivre_liberaEmailAntigo() {
        Artista existente = new Artista();
        existente.setIdArtista(1L);
        existente.setEmail("outro@teste.com");
        artista.setSenha("novaSenha");
        when(artistaRepository.findById(1L)).thenReturn(existente);
        when(emailRegistroRepository.tryReserve("artista@teste.com")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaSenhaCriptografada");
        when(artistaRepository.update(artista)).thenReturn(1);

        artistaService.update(artista);

        verify(emailRegistroRepository).release("outro@teste.com");
    }

    @Test
    void findById_delegaParaRepository() {
        when(artistaRepository.findById(1L)).thenReturn(artista);

        Artista resultado = artistaService.findById(1L);

        assertThat(resultado).isEqualTo(artista);
    }

    @Test
    void deleteById_liberaEmailNoRegistro() {
        when(artistaRepository.findById(1L)).thenReturn(artista);
        when(artistaRepository.deleteById(1L)).thenReturn(1);

        int linhasAfetadas = artistaService.deleteById(1L);

        assertThat(linhasAfetadas).isEqualTo(1);
        verify(emailRegistroRepository).release("artista@teste.com");
    }
}
