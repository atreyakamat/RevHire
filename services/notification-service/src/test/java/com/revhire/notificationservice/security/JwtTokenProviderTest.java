package com.revhire.notificationservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "RevHireSuperSecretKeyForJwtGenerationMakeItLongEnough1234567890ABCDEF");
    }

    @Test
    void testGenerateAndValidateToken_Success() {
        String token = tokenProvider.generateToken(101L, "JOB_SEEKER", "user@test.com");

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(101L, tokenProvider.getUserIdFromJWT(token));
        assertEquals("JOB_SEEKER", tokenProvider.getRoleFromJWT(token));
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        assertFalse(tokenProvider.validateToken("invalid.token"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void testGetUserIdFromJWT_NullOrBlank_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.getUserIdFromJWT(null));
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.getUserIdFromJWT("   "));
    }

    @Test
    void testGetRoleFromJWT_NullOrBlank_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.getRoleFromJWT(null));
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.getRoleFromJWT("   "));
    }
}
