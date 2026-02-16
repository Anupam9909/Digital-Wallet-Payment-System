package com.digitalwallet.api_gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestValidationFilter implements GlobalFilter, Ordered {

    private static final int MAX_URI_LENGTH = 2048;        // 2KB
    private static final int MAX_QUERY_PARAMS = 50;        // Max query parameters
    private static final int MAX_HEADER_COUNT = 100;       // Max headers count

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Validate URI length
        String uri = request.getURI().toString();
        if (uri.length() > MAX_URI_LENGTH) {
            return handleError(exchange, HttpStatus.REQUEST_URI_TOO_LONG,
                    "URI length exceeds maximum allowed: " + MAX_URI_LENGTH + " characters");
        }

        // Validate query parameters count
        int queryParamCount = request.getQueryParams().size();
        if (queryParamCount > MAX_QUERY_PARAMS) {
            return handleError(exchange, HttpStatus.BAD_REQUEST,
                    "Too many query parameters. Maximum allowed: " + MAX_QUERY_PARAMS);
        }

        // Validate headers count
        int headerCount = request.getHeaders().size();
        if (headerCount > MAX_HEADER_COUNT) {
            return handleError(exchange, HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE,
                    "Too many headers. Maximum allowed: " + MAX_HEADER_COUNT);
        }

        // Check for malformed headers
        if (hasInvalidHeaders(request)) {
            return handleError(exchange, HttpStatus.BAD_REQUEST,
                    "Malformed headers detected");
        }

        // Log request info for monitoring
        logRequestInfo(request);

        return chain.filter(exchange);
    }

    private boolean hasInvalidHeaders(ServerHttpRequest request) {
        return request.getHeaders().entrySet().stream()
                .anyMatch(entry -> {
                    // Check for null or empty header names
                    if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                        return true;
                    }
                    // Check for null header values
                    return entry.getValue().stream().anyMatch(value -> value == null);
                });
    }

    private void logRequestInfo(ServerHttpRequest request) {
        System.out.println("=== REQUEST VALIDATION ===");
        System.out.println("Method: " + request.getMethod());
        System.out.println("Path: " + request.getPath().value());
        System.out.println("URI Length: " + request.getURI().toString().length());
        System.out.println("Query Params: " + request.getQueryParams().size());
        System.out.println("Headers Count: " + request.getHeaders().size());
        System.out.println("========================");
    }

    private Mono<Void> handleError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String errorBody = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\",\"path\":\"%s\"}",
                status.getReasonPhrase(),
                message,
                java.time.Instant.now(),
                exchange.getRequest().getPath().value()
        );

        org.springframework.core.io.buffer.DataBuffer buffer =
                exchange.getResponse().bufferFactory().wrap(errorBody.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1000; // Run very early, before other filters
    }
}
