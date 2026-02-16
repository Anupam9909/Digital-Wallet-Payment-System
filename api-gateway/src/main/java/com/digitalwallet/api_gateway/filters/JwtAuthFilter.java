package com.digitalwallet.api_gateway.filters;

import com.digitalwallet.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(-1000) // Very high priority - executes before Spring Security filters (gateWay sec config)
public class JwtAuthFilter implements WebFilter {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/signup",
            "/auth/login",
            "/actuator/health",
            "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String normalizedPath = path.replaceAll("/+$", "");

        System.out.println("=== JWT WebFilter EXECUTING for: " + normalizedPath + " ===");

        // Skip JWT check for public endpoints
        if (isPublicPath(normalizedPath)) {
            System.out.println("Public endpoint, proceeding without JWT check: " + normalizedPath);
            return chain.filter(exchange);
        }

        // For protected endpoints, check JWT
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Missing or invalid Authorization header for: " + normalizedPath);
            return handleUnauthorized(exchange);
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);

            // Extract user information from JWT
            String userEmail = claims.getSubject();
            String role = claims.get("role", String.class);
            Integer userId = claims.get("userId", Integer.class);

            System.out.println("✅ JWT validated for user: " + userEmail + " with role: " + role);

            // Create authorities with ROLE_ prefix for Spring Security
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // Create Authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

            // Create SecurityContext so that Spring Security can do authorization in GatewaySec_config
            SecurityContext securityContext = new SecurityContextImpl(authentication);

            // Add custom headers for downstream services
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Email", userEmail)
                    .header("X-User-Role", role)
                    .header("X-User-Id", String.valueOf(userId))
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            System.out.println("🔄 Proceeding with authentication context set");

            // Continue with SecurityContext properly set
            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                            Mono.just(securityContext)
                    ));

        } catch (Exception e) {
            System.out.println("❌ JWT validation failed for: " + normalizedPath + " - " + e.getMessage());
            return handleUnauthorized(exchange);
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath ->
                path.equals(publicPath) ||
                        (publicPath.endsWith("/**") && path.startsWith(publicPath.substring(0, publicPath.length() - 3)))
        );
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String body = "{\"error\":\"Unauthorized\",\"message\":\"Valid JWT token required\"}";
        org.springframework.core.io.buffer.DataBuffer buffer =
                exchange.getResponse().bufferFactory().wrap(body.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
