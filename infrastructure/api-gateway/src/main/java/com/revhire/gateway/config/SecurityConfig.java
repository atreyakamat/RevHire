package com.revhire.gateway.config;

import com.revhire.gateway.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtTokenProvider tokenProvider, CorsConfigurationSource corsConfigurationSource) {
        this.tokenProvider = tokenProvider;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .securityContextRepository(new ServerSecurityContextRepository() {
                @Override
                public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
                    return Mono.empty();
                }

                @Override
                public Mono<SecurityContext> load(ServerWebExchange exchange) {
                    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        if (tokenProvider.validateToken(token)) {
                            Long userId = tokenProvider.getUserIdFromJWT(token);
                            String role = tokenProvider.getRoleFromJWT(token);

                            List<GrantedAuthority> authorities = new ArrayList<>();
                            if (role != null) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            }

                            Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                            return Mono.just(new SecurityContextImpl(auth));
                        }
                    }
                    return Mono.empty();
                }
            })
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    byte[] bytes = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}".getBytes(StandardCharsets.UTF_8);
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                })
                .accessDeniedHandler((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    byte[] bytes = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\"}".getBytes(StandardCharsets.UTF_8);
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                })
            )
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                .pathMatchers("/actuator/**").hasRole("ADMIN")
                .pathMatchers("/api/auth/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/test/ping").permitAll()
                .pathMatchers("/api/test/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/jobs").hasRole("EMPLOYER")
                .pathMatchers(HttpMethod.PUT, "/api/jobs/**").hasRole("EMPLOYER")
                .pathMatchers(HttpMethod.DELETE, "/api/jobs/**").hasAnyRole("EMPLOYER", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/applications", "/api/applications/**").hasRole("JOB_SEEKER")
                .pathMatchers(HttpMethod.PUT, "/api/applications/*/status").hasAnyRole("EMPLOYER", "ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/applications/**").hasAnyRole("JOB_SEEKER", "ADMIN")
                .pathMatchers("/api/resumes/**").hasAnyRole("JOB_SEEKER", "EMPLOYER", "ADMIN")
                .pathMatchers("/api/notifications/**").authenticated()
                .pathMatchers("/api/users/me").authenticated()
                .pathMatchers("/api/users/{id}").hasAnyRole("EMPLOYER", "ADMIN")
                .pathMatchers("/api/**").authenticated()
                .anyExchange().permitAll()
            )
            .build();
    }
}
