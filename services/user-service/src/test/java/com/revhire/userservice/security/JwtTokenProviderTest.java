package com.revhire.userservice.security;

import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-28T10:00:00Z"), ZoneId.of("UTC"));
        tokenProvider = new JwtTokenProvider(fixedClock);
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "RevHireSuperSecretKeyForJwtGenerationMakeItLongEnough1234567890ABCDEF");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 3600000); // 1 hr
    }

    @Test
    void testGenerateAndValidateToken_Success() {
        User user = new User("jwtuser@revhire.local", "pass", Role.JOB_SEEKER);
        user.setId(42L);
        CustomUserDetails userDetails = CustomUserDetails.create(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(42L, tokenProvider.getUserIdFromJWT(token));
    }

    @Test
    void testGenerateToken_NullAuth_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.generateToken(null));
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void testGetUserIdFromJWT_NullOrBlank_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.getUserIdFromJWT(null));
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.getUserIdFromJWT("   "));
    }
}
