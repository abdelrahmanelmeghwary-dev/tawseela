package com.tawseela.security;

import com.tawseela.config.TawseelaProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_MOBILE = "mobileNumber";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey key;
    private final long accessExpirationMs;

    public JwtService(TawseelaProperties props) {
        byte[] secretBytes = props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessExpirationMs = props.getJwt().getAccessExpirationMs();
    }

    public String createAccessToken(UUID userId, String mobileNumber, List<String> roles) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .id(jti)
                .subject(userId.toString())
                .claim(CLAIM_MOBILE, mobileNumber)
                .claim(CLAIM_ROLES, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpirationMs)))
                .signWith(key)
                .compact();
    }

    public ParsedAccessToken parseAccessToken(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        UUID userId = UUID.fromString(claims.getSubject());
        String mobile = claims.get(CLAIM_MOBILE, String.class);
        List<String> roles = new ArrayList<>();
        Object rawRoles = claims.get(CLAIM_ROLES);
        if (rawRoles instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) {
                    roles.add(String.valueOf(o));
                }
            }
        }
        String jti = claims.getId();
        Instant expiresAt = claims.getExpiration() != null
                ? Instant.ofEpochMilli(claims.getExpiration().getTime())
                : null;
        return new ParsedAccessToken(userId, mobile, roles, jti, expiresAt);
    }

    public static final class ParsedAccessToken {
        private final UUID userId;
        private final String mobileNumber;
        private final List<String> roles;
        private final String jti;
        private final Instant expiresAt;

        public ParsedAccessToken(
                UUID userId, String mobileNumber, List<String> roles, String jti, Instant expiresAt) {
            this.userId = userId;
            this.mobileNumber = mobileNumber;
            this.roles = roles;
            this.jti = jti;
            this.expiresAt = expiresAt;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public List<String> getRoles() {
            return roles;
        }

        public String getJti() {
            return jti;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }
    }
}
