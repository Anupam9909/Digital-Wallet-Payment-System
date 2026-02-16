package com.DigitalWalletPaymentService.user_service.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JWTRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTRequestFilter.class);

    private final JWTUtil jwtUtil;

    public JWTRequestFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return; // No token provided, skip authentication
        }

        final String jwt = authHeader.substring(7);

        if (jwt.isBlank()) {
            chain.doFilter(request, response);
            return; // Empty token, skip authentication
        }

        try {
            // Extract username/email
            String username = jwtUtil.extractUsername(jwt);
            if (username == null || username.isBlank()) {
                chain.doFilter(request, response);
                return; // Invalid token without username
            }

            // Check if already authenticated
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate token
                if (!jwtUtil.validateToken(jwt, username)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid or expired JWT token");
                    return;
                }

                // Extract role, default to USER if missing
                String role = jwtUtil.extractRole(jwt);
                if (role == null || role.isBlank()) {
                    role = "ROLE_USER";
                } else if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role.toUpperCase();
                }

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired JWT token");
        }
    }
}
