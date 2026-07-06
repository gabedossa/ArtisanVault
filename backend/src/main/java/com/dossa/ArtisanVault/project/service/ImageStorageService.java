package com.dossa.ArtisanVault.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

        Files.createDirectories(uploadDir);
        String filename = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename);
        file.transferTo(target);

        return "/uploads/portfolio/" + filename;
    }
}
