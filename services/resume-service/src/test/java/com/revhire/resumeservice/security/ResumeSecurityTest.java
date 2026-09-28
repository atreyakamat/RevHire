package com.revhire.resumeservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeSecurityTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "RevHireSuperSecretKeyForJwtGenerationMakeItLongEnough1234567890ABCDEF";

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", secret);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }

    @Test
    void testValidateToken_Valid() {
        String token = generateToken(42L);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(42L, tokenProvider.getUserIdFromJWT(token));
    }

    @Test
    void testValidateToken_InvalidOrEmpty() {
        assertFalse(tokenProvider.validateToken(null));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken("invalid.token"));
    }

    @Test
    void testGetUserIdFromJWT_NullOrEmpty_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.getUserIdFromJWT(null));
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.getUserIdFromJWT(""));
    }

    @Test
    void testJwtAuthenticationFilter_ValidToken() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = generateToken(10L);
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(10L, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void testJwtAuthenticationFilter_NoAuthHeader() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
