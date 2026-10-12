package com.example.springboot_with_jpa_crud_repo;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RevokedTokenService {
    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String token, Instant expiresAt) {
        removeExpiredTokens();
        revokedTokens.put(token, expiresAt);
    }

    public boolean isRevoked(String token) {
        Instant expiresAt = revokedTokens.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            revokedTokens.remove(token);
            return false;
        }
        return true;
    }

    private void removeExpiredTokens() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
