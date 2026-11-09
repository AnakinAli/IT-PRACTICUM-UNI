package org.example.upr5.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

public class JwtService {
    public static final String AUTH_TOKEN = "auth";
    public static final String REFRESH_TOKEN = "refresh";

    private static final String DEVELOPMENT_SECRET =
            "upr5-local-development-secret-change-me-before-production-0123456789-abcdef";

    private final javax.crypto.SecretKey key;
    private final Clock clock;

    public JwtService() {
        this(resolveSecret(), Clock.systemUTC());
    }

    JwtService(String secret, Clock clock) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 64 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.clock = clock;
    }

    public String createToken(String email, long validitySeconds, String tokenUse) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (validitySeconds <= 0) {
            throw new IllegalArgumentException("Token validity must be positive");
        }

        Instant issuedAt = clock.instant();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(issuedAt.plusSeconds(validitySeconds)))
                .claim("token_use", tokenUse)
                .signWith(key)
                .compact();
    }

    public Jws<Claims> validate(String token, String expectedTokenUse) {
        Jws<Claims> parsed = Jwts.parserBuilder()
                .setSigningKey(key)
                .setClock(() -> Date.from(clock.instant()))
                .build()
                .parseClaimsJws(token);

        String actualTokenUse = parsed.getBody().get("token_use", String.class);
        if (!expectedTokenUse.equals(actualTokenUse)) {
            throw new IllegalArgumentException("Wrong token type");
        }
        return parsed;
    }

    private static String resolveSecret() {
        String configured = System.getenv("JWT_SECRET");
        return configured == null || configured.isBlank() ? DEVELOPMENT_SECRET : configured;
    }
}
