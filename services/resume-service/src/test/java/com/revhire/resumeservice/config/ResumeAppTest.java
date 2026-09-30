package com.revhire.resumeservice.config;

import com.revhire.resumeservice.ResumeServiceApplication;
import com.revhire.resumeservice.security.JwtAuthenticationEntryPoint;
import com.revhire.resumeservice.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResumeAppTest {

    @Mock
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testSecurityConfigClock() {
        SecurityConfig config = new SecurityConfig(unauthorizedHandler, jwtAuthenticationFilter);
        assertNotNull(config.clock());
    }

    @Test
    void testResumeServiceApplicationInit() {
        ResumeServiceApplication app = new ResumeServiceApplication();
        assertNotNull(app);
    }
}
