package com.tawseela.mapper;

import com.tawseela.dto.response.AuthMeResponse;
import com.tawseela.entity.DriverProfile;
import com.tawseela.entity.Profile;
import com.tawseela.entity.User;
import com.tawseela.util.RoleNames;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public AuthMeResponse toAuthMe(User user) {
        return AuthMeResponse.builder()
                .userId(user.getId())
                .mobileNumber(user.getMobileNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(RoleNames.fromUser(user))
                .phoneVerified(user.isPhoneVerified())
                .enabled(user.isEnabled())
                .build();
    }

    public AuthMeResponse enrichWithProfile(AuthMeResponse base, Profile profile) {
        return AuthMeResponse.builder()
                .userId(base.getUserId())
                .mobileNumber(base.getMobileNumber())
                .firstName(base.getFirstName())
                .lastName(base.getLastName())
                .roles(base.getRoles())
                .phoneVerified(base.isPhoneVerified())
                .enabled(base.isEnabled())
                .fullName(profile.getFullName())
                .profilePhone(profile.getPhone())
                .fcmToken(profile.getFcmToken())
                .avatarUrl(profile.getAvatarUrl())
                .profileCreatedAt(profile.getCreatedAt())
                .profileUpdatedAt(profile.getUpdatedAt())
                .driverProfileId(base.getDriverProfileId())
                .vehicleType(base.getVehicleType())
                .vehicleNumber(base.getVehicleNumber())
                .licenseNumber(base.getLicenseNumber())
                .driverApproved(base.getDriverApproved())
                .build();
    }

    public AuthMeResponse enrichWithDriverProfile(AuthMeResponse base, DriverProfile driverProfile) {
        return AuthMeResponse.builder()
                .userId(base.getUserId())
                .mobileNumber(base.getMobileNumber())
                .firstName(base.getFirstName())
                .lastName(base.getLastName())
                .roles(base.getRoles())
                .phoneVerified(base.isPhoneVerified())
                .enabled(base.isEnabled())
                .fullName(base.getFullName())
                .profilePhone(base.getProfilePhone())
                .fcmToken(base.getFcmToken())
                .avatarUrl(base.getAvatarUrl())
                .profileCreatedAt(base.getProfileCreatedAt())
                .profileUpdatedAt(base.getProfileUpdatedAt())
                .driverProfileId(driverProfile.getId())
                .vehicleType(driverProfile.getVehicleType())
                .vehicleNumber(driverProfile.getVehicleNumber())
                .licenseNumber(driverProfile.getLicenseNumber())
                .driverApproved(driverProfile.isApproved())
                .build();
    }
}
