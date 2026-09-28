package com.revhire.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
@Component
@SuppressWarnings("java:S2143") // io.jsonwebtoken 0.11.x requires java.util.Date for setIssuedAt and setExpiration
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final Clock clock;

    @Value("${app.jwtSecret:RevHireSuperSecretKeyForJwtGenerationMakeItLongEnough1234567890ABCDEF}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs:86400000}") // 1 day
    private int jwtExpirationInMs;

    public JwtTokenProvider() {
        this(Clock.systemUTC());
    }

    @Autowired
    public JwtTokenProvider(Clock clock) {
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @SuppressWarnings("java:S2143") // io.jsonwebtoken 0.11.x requires java.util.Date for setIssuedAt and setExpiration
    public String generateToken(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userPrincipal)) {
            throw new IllegalArgumentException("Valid CustomUserDetails principal is required to generate JWT token");
        }

        Instant now = clock.instant();
        Instant expiryInstant = now.plusMillis(jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(java.util.Date.from(now))
                .setExpiration(java.util.Date.from(expiryInstant))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Long getUserIdFromJWT(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token string cannot be null or empty");
        }
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        if (authToken == null || authToken.isBlank()) {
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }
}
