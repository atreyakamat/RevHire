package com.revhire.jobservice.config;

import com.revhire.jobservice.security.JwtAuthenticationEntryPoint;
import com.revhire.jobservice.security.JwtAuthenticationFilter;
import com.revhire.jobservice.security.JwtTokenProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SuppressWarnings("java:S1075")
public class SecurityConfig {

    private static final String PATH_JOBS_ID = "/api/jobs/{id}";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_EMPLOYER = "EMPLOYER";

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler, JwtTokenProvider tokenProvider) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.GET, "/api/jobs", PATH_JOBS_ID).permitAll()
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers("/actuator/**").hasRole(ROLE_ADMIN)
                    .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole(ROLE_EMPLOYER)
                    .requestMatchers(HttpMethod.PUT, PATH_JOBS_ID).hasRole(ROLE_EMPLOYER)
                    .requestMatchers(HttpMethod.DELETE, PATH_JOBS_ID).hasAnyRole(ROLE_EMPLOYER, ROLE_ADMIN)
                    .requestMatchers(HttpMethod.GET, "/api/jobs/employer/{employerId}").hasAnyRole(ROLE_EMPLOYER, ROLE_ADMIN)
                    .anyRequest().authenticated()
                );

            http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception ex) {
            throw new org.springframework.beans.factory.BeanInitializationException("Could not configure SecurityFilterChain for job-service", ex);
        }
    }
}
