package com.dossa.ArtisanVault.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    // WEBP foi removido: o ImageIO do JDK nao tem decoder/encoder nativo para esse
    // formato, entao nao daria para validar por decodificacao nem reencodar a partir
    // dos pixels (ver secao 7 de docs/metodos-invasao-e-correcoes.md).
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif"
    );
    private static final Map<String, String> FORMAT_NAMES = Map.of(
            ".jpg", "jpg",
            ".png", "png",
            ".gif", "gif"
    );
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    private final Path uploadDir = Paths.get("uploads/portfolio");

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Imagem é obrigatória.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Imagem excede o tamanho máximo de 5MB.");
        }
        String extension = ALLOWED_CONTENT_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Formato de imagem não suportado. Use JPEG, PNG ou GIF.");
        }

        byte[] bytes = file.getBytes();
        if (!matchesMagicBytes(bytes, extension)) {
            throw new IllegalArgumentException("O conteúdo do arquivo não corresponde a uma imagem válida.");
        }

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (image == null) {
            throw new IllegalArgumentException("Arquivo não é uma imagem válida.");
        }

        // Reencoda a partir dos pixels decodificados (em vez de salvar os bytes
        // originais) para descartar metadados/payloads residuais do arquivo enviado.
        // Observação: GIFs animados são reduzidos a um único frame, já que
        // BufferedImage não preserva animação.
        ByteArrayOutputStream reencoded = new ByteArrayOutputStream();
        ImageIO.write(image, FORMAT_NAMES.get(extension), reencoded);

        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename);
        Files.write(target, reencoded.toByteArray());

        return "/uploads/portfolio/" + filename;
    }

    private boolean matchesMagicBytes(byte[] bytes, String extension) {
        switch (extension) {
            case ".jpg":
                return bytes.length >= 3
                        && (bytes[0] & 0xFF) == 0xFF
                        && (bytes[1] & 0xFF) == 0xD8
                        && (bytes[2] & 0xFF) == 0xFF;
            case ".png":
                return bytes.length >= 8
                        && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                        && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A;
            case ".gif":
                return bytes.length >= 6
                        && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                        && bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a';
            default:
                return false;
        }
    }
}
