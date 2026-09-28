package com.revhire.userservice.security;

import com.revhire.userservice.entity.Role;
import com.revhire.userservice.entity.User;
import com.revhire.userservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken("valid.token")).thenReturn(true);
        when(tokenProvider.getUserIdFromJWT("valid.token")).thenReturn(10L);

        User user = new User("user@revhire.com", "pass", Role.JOB_SEEKER);
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(10L, ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
    }

    @Test
    void testDoFilterInternal_NoHeader_PassesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_InvalidToken_PassesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken("invalid.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_ExceptionHandledGracefully() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer error.token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken("error.token")).thenThrow(new RuntimeException("Token error"));

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
