package com.tawseela.security;

import com.tawseela.enums.SystemRole;
import com.tawseela.exception.UnauthorizedActionException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID requireUserId() {
        return requireUserDetails().getUser().getId();
    }

    public static TawseelaUserDetails requireUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof TawseelaUserDetails details) {
            return details;
        }
        throw new UnauthorizedActionException("Authentication required");
    }

    public static boolean hasRole(SystemRole role) {
        String expected = "ROLE_" + role.name();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return authorities.contains(expected);
    }

    public static boolean isAdmin() {
        return hasRole(SystemRole.ADMIN);
    }
}
