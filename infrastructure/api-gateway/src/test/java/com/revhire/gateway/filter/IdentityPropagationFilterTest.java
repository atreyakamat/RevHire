package com.revhire.gateway.filter;

import com.revhire.gateway.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityPropagationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    private IdentityPropagationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new IdentityPropagationFilter(tokenProvider);
    }

    @Test
    void testFilter_StripsInjectedHeadersWhenNoToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/jobs")
                .header("X-User-Id", "99")
                .header("X-User-Role", "ADMIN")
                .header("X-User-Email", "attacker@test.com")
                .header("X-Internal-Service-Key", "spoofed-key")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> filteredExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            filteredExchange.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertNotNull(filteredExchange.get());
        HttpHeaders headers = filteredExchange.get().getRequest().getHeaders();
        assertNull(headers.getFirst("X-User-Id"));
        assertNull(headers.getFirst("X-User-Role"));
        assertNull(headers.getFirst("X-User-Email"));
        assertNull(headers.getFirst("X-Internal-Service-Key"));
    }

    @Test
    void testFilter_StripsInjectedHeadersAndPopulatesValidatedClaims() {
        String token = "valid.jwt.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUserIdFromJWT(token)).thenReturn(42L);
        when(tokenProvider.getRoleFromJWT(token)).thenReturn("EMPLOYER");
        when(tokenProvider.getEmailFromJWT(token)).thenReturn("employer@revhire.com");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/jobs")
                .header("X-User-Id", "999") // attacker attempt
                .header("X-User-Role", "ADMIN") // attacker attempt
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> filteredExchange = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            filteredExchange.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertNotNull(filteredExchange.get());
        HttpHeaders headers = filteredExchange.get().getRequest().getHeaders();
        assertEquals("42", headers.getFirst("X-User-Id"));
        assertEquals("EMPLOYER", headers.getFirst("X-User-Role"));
        assertEquals("employer@revhire.com", headers.getFirst("X-User-Email"));
    }
}
