package com.dossa.ArtisanVault.project.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

// Contador compartilhado entre instancias via Redis (ZSET com timestamp como score),
// para quando o backend roda atras de um load balancer com multiplas instancias.
// Ativado com app.rate-limit.store=redis (env RATE_LIMIT_STORE=redis); requer um
// Redis alcancavel em spring.data.redis.host/port.
@Service
@ConditionalOnProperty(prefix = "app.rate-limit", name = "store", havingValue = "redis")
public class RedisLoginRateLimiterService implements LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final String KEY_PREFIX = "artisanvault:login-rate-limit:";

    private final StringRedisTemplate redisTemplate;

    public RedisLoginRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isBlocked(String email, String ip) {
        return isBlocked("email:" + email) || isBlocked("ip:" + ip);
    }

    @Override
    public void registerAttempt(String email, String ip) {
        registerAttempt("email:" + email);
        registerAttempt("ip:" + ip);
    }

    @Override
    public void reset(String email, String ip) {
        redisTemplate.delete(KEY_PREFIX + "email:" + email);
        redisTemplate.delete(KEY_PREFIX + "ip:" + ip);
    }

    private boolean isBlocked(String key) {
        String redisKey = KEY_PREFIX + key;
        purgeExpired(redisKey);
        Long count = redisTemplate.opsForZSet().zCard(redisKey);
        return count != null && count >= MAX_ATTEMPTS;
    }

    private void registerAttempt(String key) {
        String redisKey = KEY_PREFIX + key;
        purgeExpired(redisKey);
        long now = System.currentTimeMillis();
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        zSetOps.add(redisKey, UUID.randomUUID().toString(), now);
        redisTemplate.expire(redisKey, WINDOW);
    }

    private void purgeExpired(String redisKey) {
        long cutoff = System.currentTimeMillis() - WINDOW.toMillis();
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, cutoff);
    }
}
