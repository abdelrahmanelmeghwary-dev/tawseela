package com.tawseela.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthMeResponse {

    UUID userId;
    String mobileNumber;
    String firstName;
    String lastName;
    List<String> roles;
    boolean phoneVerified;
    boolean enabled;

    String fullName;
    String profilePhone;
    String fcmToken;
    String avatarUrl;
    Instant profileCreatedAt;
    Instant profileUpdatedAt;

    UUID driverProfileId;
    String vehicleType;
    String vehicleNumber;
    String licenseNumber;
    Boolean driverApproved;
}
