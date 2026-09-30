package com.revhire.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret:${JWT_SECRET:RevHireSuperSecretKeyForJwtGenerationMakeItLongEnough1234567890ABCDEF}}")
    private String jwtSecret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object userIdObj = claims.get("userId");
        if (userIdObj != null) {
            try {
                return Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }

        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            log.debug("JWT subject is not a numeric userId: {}", claims.getSubject());
            return null;
        }
    }

    public String getRoleFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }

    public String getEmailFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.get("email", String.class);
        if (email != null) {
            return email;
        }
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
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
