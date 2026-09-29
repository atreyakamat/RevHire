package com.revhire.applicationservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationFeignConfig {

    @Value("${revhire.internal.secret:${INTERNAL_SERVICE_SECRET}}")
    private String internalServiceKey;

    @Bean
    public RequestInterceptor notificationInternalServiceRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Internal-Service-Key", internalServiceKey);
        };
    }
}
