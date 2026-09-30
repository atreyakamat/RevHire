package com.revhire.gateway.filter;

import com.revhire.gateway.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class IdentityPropagationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(IdentityPropagationFilter.class);

    private final JwtTokenProvider tokenProvider;

    public IdentityPropagationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Strip client-supplied identity headers to prevent header injection attacks
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Role");
                    headers.remove("X-User-Email");
                    headers.remove("X-Internal-Service-Key");
                    headers.remove("X-Internal-Service");
                });

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenProvider.validateToken(token)) {
                Long userId = tokenProvider.getUserIdFromJWT(token);
                String role = tokenProvider.getRoleFromJWT(token);
                String email = tokenProvider.getEmailFromJWT(token);

                if (userId != null) {
                    requestBuilder.header("X-User-Id", String.valueOf(userId));
                }
                if (role != null) {
                    requestBuilder.header("X-User-Role", role);
                }
                if (email != null) {
                    requestBuilder.header("X-User-Email", email);
                }
                log.debug("Propagating authenticated identity: userId={}, role={}, email={}", userId, role, email);
            }
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
