package com.dossa.ArtisanVault.project.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTokenBlocklistServiceTest {

    private final InMemoryTokenBlocklistService blocklist = new InMemoryTokenBlocklistService();

    @Test
    void tokenNaoBloqueadoPorPadrao() {
        assertThat(blocklist.isBlacklisted("jti-qualquer")).isFalse();
    }

    @Test
    void tokenBloqueadoAteAExpiracaoInformada() {
        long expiraEmBreve = System.currentTimeMillis() + 60_000;
        blocklist.blacklist("jti-1", expiraEmBreve);

        assertThat(blocklist.isBlacklisted("jti-1")).isTrue();
    }

    @Test
    void tokenComExpiracaoNoPassadoNaoContaComoBloqueado() {
        long jaExpirou = System.currentTimeMillis() - 1_000;
        blocklist.blacklist("jti-2", jaExpirou);

        assertThat(blocklist.isBlacklisted("jti-2")).isFalse();
    }

    @Test
    void jtisDiferentesNaoSeInterferem() {
        blocklist.blacklist("jti-3", System.currentTimeMillis() + 60_000);

        assertThat(blocklist.isBlacklisted("jti-outro")).isFalse();
    }
}
