package com.dossa.ArtisanVault.project.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(prefix = "app.token-blocklist", name = "store", havingValue = "memory", matchIfMissing = true)
public class InMemoryTokenBlocklistService implements TokenBlocklistService {

    private final ConcurrentHashMap<String, Long> expiryByJti = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String jti, long expirationEpochMillis) {
        purgeExpired();
        expiryByJti.put(jti, expirationEpochMillis);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        Long expiry = expiryByJti.get(jti);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    private void purgeExpired() {
        long now = System.currentTimeMillis();
        expiryByJti.values().removeIf(expiry -> expiry <= now);
    }
}
