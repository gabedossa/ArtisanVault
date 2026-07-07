package com.dossa.ArtisanVault.project.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@ConditionalOnProperty(prefix = "app.token-blocklist", name = "store", havingValue = "redis")
public class RedisTokenBlocklistService implements TokenBlocklistService {

    private static final String KEY_PREFIX = "artisanvault:token-blocklist:";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlocklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklist(String jti, long expirationEpochMillis) {
        long ttlMillis = expirationEpochMillis - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
    }
}
