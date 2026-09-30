package com.revhire.notificationservice.config;

import com.revhire.notificationservice.security.JwtAuthenticationEntryPoint;
import com.revhire.notificationservice.security.JwtAuthenticationFilter;
import com.revhire.notificationservice.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Test
    void testFilterRegistration() {
        SecurityConfig config = new SecurityConfig(unauthorizedHandler, tokenProvider, "test-secret");
        JwtAuthenticationFilter filter = config.jwtAuthenticationFilter();
        assertNotNull(filter);

        FilterRegistrationBean<JwtAuthenticationFilter> registration = config.jwtFilterRegistration(filter);
        assertNotNull(registration);
    }
}
