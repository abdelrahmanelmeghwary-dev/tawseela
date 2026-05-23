package com.tawseela.security;

import com.tawseela.entity.DriverProfile;
import com.tawseela.enums.SystemRole;
import com.tawseela.entity.User;
import com.tawseela.util.RoleNames;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsFactory {

    public TawseelaUserDetails build(User user, Optional<DriverProfile> driverProfile) {
        boolean driverApproved = true;
        if (RoleNames.hasRole(user, SystemRole.DRIVER)) {
            driverApproved = driverProfile.map(DriverProfile::isApproved).orElse(false);
        }
        return new TawseelaUserDetails(user, driverApproved);
    }
}
