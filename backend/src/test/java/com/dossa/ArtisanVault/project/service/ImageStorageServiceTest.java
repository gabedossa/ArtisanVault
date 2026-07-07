package com.dossa.ArtisanVault.project.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class ImageStorageServiceTest {

    // PNG 1x1 real, valido tanto na assinatura binaria quanto na decodificacao via ImageIO.
    private static final byte[] PNG_1X1 = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    private final ImageStorageService service = new ImageStorageService();

    @Test
    void store_conteudoQueNaoEUmaImagem_lancaExcecao() {
        MockMultipartFile file = new MockMultipartFile(
                "imagem", "fake.jpg", "image/jpeg",
                "isto nao e uma imagem de verdade".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void store_pngValido_naoLancaExcecao() {
        MockMultipartFile file = new MockMultipartFile(
                "imagem", "real.png", "image/png", PNG_1X1);

        assertThatCode(() -> service.store(file)).doesNotThrowAnyException();
    }

    @Test
    void store_contentTypeNaoSuportado_lancaExcecao() {
        MockMultipartFile file = new MockMultipartFile(
                "imagem", "arquivo.svg", "image/svg+xml", PNG_1X1);

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void store_arquivoVazio_lancaExcecao() {
        MockMultipartFile file = new MockMultipartFile(
                "imagem", "vazio.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
