package com.tawseela.security;

import com.tawseela.config.TawseelaProperties;
import com.tawseela.domain.Profile;
import com.tawseela.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_PHONE = "phone";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYP = "typ";
    private static final String TYP_ACCESS = "access";
    private static final String TYP_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(TawseelaProperties props) {
        byte[] secretBytes = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("tawseela.jwt.secret must be at least 32 bytes (256 bits) for HS256");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessExpirationMs = props.jwt().accessExpirationMs();
        this.refreshExpirationMs = props.jwt().refreshExpirationMs();
    }

    public String createAccessToken(Profile profile) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(profile.getId().toString())
                .claim(CLAIM_PHONE, profile.getPhone())
                .claim(CLAIM_ROLE, profile.getRole().name())
                .claim(CLAIM_TYP, TYP_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpirationMs)))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_TYP, TYP_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshExpirationMs)))
                .signWith(key)
                .compact();
    }

    public ParsedAccessToken parseAccessToken(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!TYP_ACCESS.equals(claims.get(CLAIM_TYP, String.class))) {
            throw new JwtException("Not an access token");
        }
        UUID id = UUID.fromString(claims.getSubject());
        String phone = claims.get(CLAIM_PHONE, String.class);
        Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
        return new ParsedAccessToken(id, phone, role);
    }

    public UUID parseRefreshTokenUserId(String token) throws JwtException {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!TYP_REFRESH.equals(claims.get(CLAIM_TYP, String.class))) {
            throw new JwtException("Not a refresh token");
        }
        return UUID.fromString(claims.getSubject());
    }

    public record ParsedAccessToken(UUID userId, String phone, Role role) {}
}
