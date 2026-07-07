package com.dossa.ArtisanVault.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
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
            throw new IllegalArgumentException("Formato de imagem não suportado. Use JPEG, PNG, WEBP ou GIF.");
        }

        byte[] bytes = file.getBytes();
        if (!matchesMagicBytes(bytes, extension)) {
            throw new IllegalArgumentException("O conteúdo do arquivo não corresponde a uma imagem válida.");
        }
        // WEBP não possui decoder nativo no ImageIO do JDK; para os demais formatos,
        // exigimos que o conteúdo seja realmente decodificável como imagem.
        if (!".webp".equals(extension)) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new IllegalArgumentException("Arquivo não é uma imagem válida.");
            }
        }

        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename);
        Files.write(target, bytes);

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
            case ".webp":
                return bytes.length >= 12
                        && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                        && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
            default:
                return false;
        }
    }
}
