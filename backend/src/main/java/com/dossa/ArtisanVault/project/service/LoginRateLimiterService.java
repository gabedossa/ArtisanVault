package com.dossa.ArtisanVault.project.service;

import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 5 * 60 * 1000L;

    private final ConcurrentHashMap<String, Deque<Long>> attemptsByKey = new ConcurrentHashMap<>();

    public boolean isBlocked(String email, String ip) {
        return isBlocked("email:" + email) || isBlocked("ip:" + ip);
    }

    public void registerAttempt(String email, String ip) {
        registerAttempt("email:" + email);
        registerAttempt("ip:" + ip);
    }

    public void reset(String email, String ip) {
        attemptsByKey.remove("email:" + email);
        attemptsByKey.remove("ip:" + ip);
    }

    private boolean isBlocked(String key) {
        Deque<Long> timestamps = attemptsByKey.get(key);
        if (timestamps == null) {
            return false;
        }
        synchronized (timestamps) {
            purgeExpired(timestamps);
            return timestamps.size() >= MAX_ATTEMPTS;
        }
    }

    private void registerAttempt(String key) {
        Deque<Long> timestamps = attemptsByKey.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (timestamps) {
            purgeExpired(timestamps);
            timestamps.addLast(System.currentTimeMillis());
        }
    }

    private void purgeExpired(Deque<Long> timestamps) {
        long now = System.currentTimeMillis();
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
            timestamps.pollFirst();
        }
    }
}
