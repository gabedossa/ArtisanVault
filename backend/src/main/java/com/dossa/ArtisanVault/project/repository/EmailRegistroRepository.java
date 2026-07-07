package com.dossa.ArtisanVault.project.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Uma linha por e-mail em uso por qualquer cliente ou artista. O UNIQUE real
// desta tabela (nao replicavel entre cliente/artista, que sao tabelas
// separadas) e o que de fato impede a colisao de e-mail sob concorrencia -
// reserve()/release() devem sempre ser chamados dentro da mesma transacao do
// INSERT/UPDATE/DELETE em cliente ou artista (ver ClienteService/ArtistaService).
@Repository
public class EmailRegistroRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Retorna true se o e-mail foi reservado agora; false se ja estava em uso.
    public boolean tryReserve(String email) {
        try {
            jdbcTemplate.update("INSERT INTO email_registro (email) VALUES (?)", email);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    public void release(String email) {
        jdbcTemplate.update("DELETE FROM email_registro WHERE email = ?", email);
    }
}
