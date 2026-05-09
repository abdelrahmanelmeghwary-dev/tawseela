package com.tawseela.security;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * In-memory denylist for access token {@code jti} (logout / revocation). For multi-node production,
 * replace with Redis or similar.
 */
@Component
public class AccessTokenBlacklist {

    private final ConcurrentHashMap<String, Instant> deniedUntil = new ConcurrentHashMap<String, Instant>();

    public void denyUntil(String jti, Instant expiresAt) {
        if (jti == null || jti.isEmpty()) {
            return;
        }
        deniedUntil.put(jti, expiresAt);
    }

    public boolean isDenied(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }
        Instant until = deniedUntil.get(jti);
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            deniedUntil.remove(jti);
            return false;
        }
        return true;
    }

    /** Best-effort cleanup of expired entries (bounded work per call). */
    public void purgeExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> it = deniedUntil.entrySet().iterator();
        int removed = 0;
        while (it.hasNext() && removed < 512) {
            Map.Entry<String, Instant> e = it.next();
            if (now.isAfter(e.getValue())) {
                it.remove();
                removed++;
            }
        }
    }
}
