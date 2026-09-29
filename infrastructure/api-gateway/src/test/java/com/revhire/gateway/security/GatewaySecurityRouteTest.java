package com.revhire.gateway.security;

import com.revhire.gateway.config.CorsConfig;
import com.revhire.gateway.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewaySecurityRouteTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    private WebTestClient client;

    @RestController
    static class DummyTestController {
        @GetMapping("/api/auth/login")
        public String login() { return "login"; }

        @GetMapping("/api/jobs")
        public String getJobs() { return "jobs"; }

        @PostMapping("/api/jobs")
        public String createJob() { return "created"; }

        @GetMapping("/api/users/me")
        public String me() { return "me"; }

        @GetMapping("/api/test/ping")
        public String ping() { return "pong"; }

        @GetMapping("/api/test/records")
        public String records() { return "records"; }

        @org.springframework.web.bind.annotation.PutMapping("/api/applications/1/status")
        public String status() { return "status"; }
    }

    @BeforeEach
    void setUp() {
        CorsConfig corsConfig = new CorsConfig();
        SecurityConfig securityConfig = new SecurityConfig(tokenProvider, corsConfig.corsConfigurationSource());

        client = WebTestClient.bindToController(new DummyTestController())
                .apply(SecurityMockServerConfigurers.springSecurity())
                .webFilter(new org.springframework.security.web.server.WebFilterChainProxy(
                        securityConfig.springSecurityFilterChain(
                                org.springframework.security.config.web.server.ServerHttpSecurity.http()
                        )
                ))
                .build();
    }

    @Test
    void testPublicRoutesPermitted() {
        client.get().uri("/api/jobs")
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/api/auth/login")
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/api/test/ping")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testProtectedRoutesRequireAuth() {
        client.get().uri("/api/users/me")
                .exchange()
                .expectStatus().isUnauthorized();

        client.post().uri("/api/jobs")
                .exchange()
                .expectStatus().isUnauthorized();

        client.get().uri("/api/test/records")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testRoleRestrictedRoutes() {
        when(tokenProvider.validateToken("seeker.token")).thenReturn(true);
        when(tokenProvider.getUserIdFromJWT("seeker.token")).thenReturn(10L);
        when(tokenProvider.getRoleFromJWT("seeker.token")).thenReturn("JOB_SEEKER");

        // Seeker cannot create jobs (requires EMPLOYER) -> 403 Forbidden
        client.post().uri("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer seeker.token")
                .exchange()
                .expectStatus().isForbidden();

        // Seeker cannot update application status (requires EMPLOYER or ADMIN) -> 403 Forbidden
        client.put().uri("/api/applications/1/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer seeker.token")
                .exchange()
                .expectStatus().isForbidden();

        // Seeker cannot access test CRUD endpoints (requires ADMIN) -> 403 Forbidden
        client.get().uri("/api/test/records")
                .header(HttpHeaders.AUTHORIZATION, "Bearer seeker.token")
                .exchange()
                .expectStatus().isForbidden();

        when(tokenProvider.validateToken("employer.token")).thenReturn(true);
        when(tokenProvider.getUserIdFromJWT("employer.token")).thenReturn(20L);
        when(tokenProvider.getRoleFromJWT("employer.token")).thenReturn("EMPLOYER");

        // Employer can create jobs -> 200 OK
        client.post().uri("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer employer.token")
                .exchange()
                .expectStatus().isOk();

        // Employer can update application status -> 200 OK
        client.put().uri("/api/applications/1/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer employer.token")
                .exchange()
                .expectStatus().isOk();

        when(tokenProvider.validateToken("admin.token")).thenReturn(true);
        when(tokenProvider.getUserIdFromJWT("admin.token")).thenReturn(1L);
        when(tokenProvider.getRoleFromJWT("admin.token")).thenReturn("ADMIN");

        // Admin can access test service CRUD -> 200 OK
        client.get().uri("/api/test/records")
                .header(HttpHeaders.AUTHORIZATION, "Bearer admin.token")
                .exchange()
                .expectStatus().isOk();
    }
}
