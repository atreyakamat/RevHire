package com.revhire.userservice.config;

import com.revhire.userservice.UserServiceApplication;
import com.revhire.userservice.security.JwtAuthenticationEntryPoint;
import com.revhire.userservice.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    void testPasswordEncoder() {
        SecurityConfig config = new SecurityConfig(unauthorizedHandler, jwtAuthenticationFilter);
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder.matches("test", encoder.encode("test")));
    }

    @Test
    void testAuthenticationManager() {
        SecurityConfig config = new SecurityConfig(unauthorizedHandler, jwtAuthenticationFilter);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager manager = config.authenticationManager(authenticationConfiguration);
        assertNotNull(manager);
    }

    @Test
    void testUserServiceApplication() {
        UserServiceApplication app = new UserServiceApplication();
        assertNotNull(app);
        assertNotNull(app.clock());
    }
}
