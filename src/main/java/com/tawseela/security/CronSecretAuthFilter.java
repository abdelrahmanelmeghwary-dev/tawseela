package com.tawseela.security;

import com.tawseela.config.TawseelaProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CronSecretAuthFilter extends OncePerRequestFilter {

    public static final String CRON_SECRET_HEADER = "X-Cron-Secret";

    private final TawseelaProperties properties;

    public CronSecretAuthFilter(TawseelaProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/system/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String secret = request.getHeader(CRON_SECRET_HEADER);
        String expected = properties.getDelivery().getCronSecret();
        if (StringUtils.hasText(expected) && expected.equals(secret)) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "cron", null, List.of(new SimpleGrantedAuthority("ROLE_CRON")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
