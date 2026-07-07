package com.dossa.ArtisanVault.project.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

class ImageStorageServiceTest {

    // PNG 1x1 real, valido tanto na assinatura binaria quanto na decodificacao via ImageIO.
    private static final byte[] PNG_1X1 = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    private final ImageStorageService service = new ImageStorageService();
    private final List<Path> arquivosCriados = new ArrayList<>();

    @AfterEach
    void limparArquivosCriados() throws IOException {
        for (Path path : arquivosCriados) {
            Files.deleteIfExists(path);
        }
    }

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

    @Test
    void store_reencodaImagem_removendoDadosAnexadosAposOFinalDoPng() throws IOException {
        byte[] payloadEscondido = "conteudo-nao-esperado-anexado-ao-arquivo".getBytes();
        byte[] pngComPayloadAnexado = new byte[PNG_1X1.length + payloadEscondido.length];
        System.arraycopy(PNG_1X1, 0, pngComPayloadAnexado, 0, PNG_1X1.length);
        System.arraycopy(payloadEscondido, 0, pngComPayloadAnexado, PNG_1X1.length, payloadEscondido.length);

        MockMultipartFile file = new MockMultipartFile(
                "imagem", "polyglot.png", "image/png", pngComPayloadAnexado);

        String urlRelativa = service.store(file);
        Path arquivoSalvo = Paths.get(urlRelativa.substring(1));
        arquivosCriados.add(arquivoSalvo);

        byte[] conteudoSalvo = Files.readAllBytes(arquivoSalvo);
        String conteudoComoTexto = new String(conteudoSalvo);

        assertThat(conteudoComoTexto).doesNotContain("conteudo-nao-esperado-anexado-ao-arquivo");
    }
}
