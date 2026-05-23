package com.tawseela.security;

import com.tawseela.entity.RoleEntity;
import com.tawseela.enums.SystemRole;
import com.tawseela.entity.User;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class TawseelaUserDetails implements UserDetails {

    private final User user;
    private final boolean driverApproved;

    public TawseelaUserDetails(User user, boolean driverApproved) {
        this.user = user;
        this.driverApproved = driverApproved;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(RoleEntity::getName)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getMobileNumber();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (!user.isPhoneVerified()) {
            return false;
        }
        if (!user.isEnabled()) {
            return false;
        }
        if (hasDriverRole() && !driverApproved) {
            return false;
        }
        return true;
    }

    private boolean hasDriverRole() {
        for (RoleEntity r : user.getRoles()) {
            if (r.getName() == SystemRole.DRIVER) {
                return true;
            }
        }
        return false;
    }
}
