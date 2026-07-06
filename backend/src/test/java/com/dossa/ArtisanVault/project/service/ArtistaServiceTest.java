package com.dossa.ArtisanVault.project.service;

import com.dossa.ArtisanVault.project.entity.Artista;
import com.dossa.ArtisanVault.project.repository.ArtistaRepository;
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
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository artistaRepository;

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
        when(passwordEncoder.encode("senhaEmTexto")).thenReturn("senhaCriptografada");
        when(artistaRepository.save(artista)).thenReturn(1);

        int linhasAfetadas = artistaService.save(artista);

        assertThat(artista.getSenha()).isEqualTo("senhaCriptografada");
        assertThat(linhasAfetadas).isEqualTo(1);
        verify(artistaRepository).save(artista);
    }

    @Test
    void update_comSenhaEmBranco_mantemSenhaExistente() {
        artista.setSenha("");
        Artista existente = new Artista();
        existente.setIdArtista(1L);
        existente.setSenha("senhaAntigaCriptografada");
        when(artistaRepository.findById(1L)).thenReturn(existente);
        when(artistaRepository.update(artista)).thenReturn(1);

        artistaService.update(artista);

        assertThat(artista.getSenha()).isEqualTo("senhaAntigaCriptografada");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void update_comNovaSenha_criptografaNovaSenha() {
        artista.setSenha("novaSenha");
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaSenhaCriptografada");
        when(artistaRepository.update(artista)).thenReturn(1);

        artistaService.update(artista);

        assertThat(artista.getSenha()).isEqualTo("novaSenhaCriptografada");
        verify(artistaRepository, never()).findById(anyLong());
    }

    @Test
    void findById_delegaParaRepository() {
        when(artistaRepository.findById(1L)).thenReturn(artista);

        Artista resultado = artistaService.findById(1L);

        assertThat(resultado).isEqualTo(artista);
    }
}
