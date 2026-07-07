package com.dossa.ArtisanVault.project.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterServiceTest {

    private final LoginRateLimiterService rateLimiter = new LoginRateLimiterService();

    @Test
    void apos5TentativasComOMesmoEmail_bloqueiaMesmoComIpsDiferentes() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.registerAttempt("alvo@teste.com", "ip-" + i);
        }

        assertThat(rateLimiter.isBlocked("alvo@teste.com", "ip-novo")).isTrue();
    }

    @Test
    void apos5TentativasDoMesmoIp_bloqueiaMesmoComEmailsDiferentes() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.registerAttempt("email" + i + "@teste.com", "1.2.3.4");
        }

        assertThat(rateLimiter.isBlocked("outro-email@teste.com", "1.2.3.4")).isTrue();
    }

    @Test
    void comMenosDe5Tentativas_naoBloqueia() {
        rateLimiter.registerAttempt("alvo2@teste.com", "1.1.1.1");
        rateLimiter.registerAttempt("alvo2@teste.com", "1.1.1.1");

        assertThat(rateLimiter.isBlocked("alvo2@teste.com", "1.1.1.1")).isFalse();
    }

    @Test
    void reset_limpaOContadorParaAquelaChave() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.registerAttempt("alvo3@teste.com", "2.2.2.2");
        }
        assertThat(rateLimiter.isBlocked("alvo3@teste.com", "2.2.2.2")).isTrue();

        rateLimiter.reset("alvo3@teste.com", "2.2.2.2");

        assertThat(rateLimiter.isBlocked("alvo3@teste.com", "2.2.2.2")).isFalse();
    }

    @Test
    void chavesDiferentesNaoSeInterferem() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.registerAttempt("vitima@teste.com", "9.9.9.9");
        }

        assertThat(rateLimiter.isBlocked("outra-pessoa@teste.com", "8.8.8.8")).isFalse();
    }
}
