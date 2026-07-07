package com.dossa.ArtisanVault.project.security;

// JWTs sao stateless: por padrao, um token continua valido (via Authorization
// header) ate expirar, mesmo depois de "logout" (que so limpa o cookie no
// navegador). Este blocklist registra o jti dos tokens invalidados por logout
// ate a expiracao original do token, fechando essa janela.
//
// Implementacao escolhida via app.token-blocklist.store (env
// TOKEN_BLOCKLIST_STORE): "memory" (default, InMemoryTokenBlocklistService) ou
// "redis" (RedisTokenBlocklistService, compartilhado entre multiplas
// instancias do backend) - mesmo padrao usado por LoginRateLimiterService.
public interface TokenBlocklistService {

    void blacklist(String jti, long expirationEpochMillis);

    boolean isBlacklisted(String jti);
}
