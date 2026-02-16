package com.digitalwallet.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    /**
     * Custom KeyResolver for rate limiting
     * Primary strategy: Rate limit by X-User-Id header
     * Fallback strategy: Rate limit by client IP address
     */
    @Bean
    @Primary
    public KeyResolver userIdKeyResolver() {
        return (exchange) -> {
            // First, try to get User ID from X-User-Id header (set by JWT filter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

            if (userId != null && !userId.trim().isEmpty()) {
                return Mono.just(userId);
            }

            // Fallback to IP-based rate limiting
            return Mono.just( Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
        };
    }
}
