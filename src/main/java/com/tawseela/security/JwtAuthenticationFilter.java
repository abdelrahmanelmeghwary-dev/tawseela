package com.tawseela.security;

import com.tawseela.entity.User;
import com.tawseela.repository.DriverProfileRepository;
import com.tawseela.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_POST_PATHS = new HashSet<String>(Arrays.asList(
            "/api/auth/register",
            "/api/auth/register/verify",
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api/auth/otp/send",
            "/api/auth/otp/verify",
            "/api/auth/forgot-password/send-otp",
            "/api/auth/forgot-password/verify-otp",
            "/api/auth/forgot-password/reset"));

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserDetailsFactory userDetailsFactory;
    private final AccessTokenBlacklist accessTokenBlacklist;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            DriverProfileRepository driverProfileRepository,
            UserDetailsFactory userDetailsFactory,
            AccessTokenBlacklist accessTokenBlacklist) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.userDetailsFactory = userDetailsFactory;
        this.accessTokenBlacklist = accessTokenBlacklist;
    }

    private static boolean isPublicPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && PUBLIC_POST_PATHS.contains(request.getServletPath());
    }

    private static boolean isMachineAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_CRON".equals(role) || "ROLE_SERVICE".equals(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (isMachineAuthentication(existing)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String raw = header.substring(7).trim();
            if (!raw.isEmpty()) {
                try {
                    JwtService.ParsedAccessToken parsed = jwtService.parseAccessToken(raw);
                    if (accessTokenBlacklist.isDenied(parsed.getJti())) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token has been revoked");
                        return;
                    }
                    User user = userRepository
                            .findByIdEagerRoles(parsed.getUserId())
                            .orElseThrow(() -> new JwtException("User not found"));
                    UserDetails details =
                            userDetailsFactory.build(user, driverProfileRepository.findByUser_Id(user.getId()));
                    if (!details.isEnabled()) {
                        response.sendError(
                                HttpServletResponse.SC_FORBIDDEN, "Account not verified or pending approval");
                        return;
                    }
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (JwtException ex) {
                    if (!isPublicPost(request)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

