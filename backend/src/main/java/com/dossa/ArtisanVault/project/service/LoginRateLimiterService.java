package com.dossa.ArtisanVault.project.service;

// Implementacao escolhida via app.rate-limit.store (env RATE_LIMIT_STORE): "memory"
// (default, InMemoryLoginRateLimiterService) ou "redis" (RedisLoginRateLimiterService,
// contador compartilhado entre multiplas instancias do backend).
public interface LoginRateLimiterService {

    boolean isBlocked(String email, String ip);

    void registerAttempt(String email, String ip);

    void reset(String email, String ip);
}
