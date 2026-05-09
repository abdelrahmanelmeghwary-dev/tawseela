package com.tawseela.security;

import com.tawseela.entity.DriverProfile;
import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.SystemRole;
import com.tawseela.entity.User;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsFactory {

    public TawseelaUserDetails build(User user, Optional<DriverProfile> driverProfile) {
        boolean driverApproved = true;
        if (hasRole(user, SystemRole.DRIVER)) {
            driverApproved = driverProfile.map(DriverProfile::isApproved).orElse(false);
        }
        return new TawseelaUserDetails(user, driverApproved);
    }

    private static boolean hasRole(User user, SystemRole role) {
        for (RoleEntity r : user.getRoles()) {
            if (r.getName() == role) {
                return true;
            }
        }
        return false;
    }
}
