package com.tawseela.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_AUTH_POST_PATHS = Set.of(
            "/api/v1/auth/otp/request",
            "/api/v1/auth/otp/verify",
            "/api/v1/auth/token/refresh");

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    private static boolean isPublicAuthPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && PUBLIC_AUTH_POST_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String raw = header.substring(7).trim();
            if (!raw.isEmpty()) {
                try {
                    JwtService.ParsedAccessToken parsed = jwtService.parseAccessToken(raw);
                    JwtPrincipal principal = new JwtPrincipal(parsed.userId(), parsed.phone(), parsed.role());
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (JwtException ex) {
                    if (!isPublicAuthPost(request)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                        return;
                    }
                    // Public OTP/refresh: ignore bad Bearer so login still works
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
