package com.revhire.notificationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret:${JWT_SECRET:RevHireSuperSecretKeyForJwtGenerationMakeItLongEnough1234567890ABCDEF}}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
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

    public String getRoleFromJWT(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token string cannot be null or empty");
        }
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
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

    @SuppressWarnings({"java:S6913", "java:S2143"}) // io.jsonwebtoken 0.11.x requires java.util.Date for setIssuedAt and setExpiration
    public String generateToken(Long userId, String role, String email) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(1, ChronoUnit.DAYS);

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .claim("role", role)
                .claim("email", email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }
}
