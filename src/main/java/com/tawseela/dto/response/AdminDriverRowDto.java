package com.tawseela.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDriverRowDto {

    private UUID driverProfileId;
    private UUID userId;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    private boolean userEnabled;
    private boolean phoneVerified;
    private List<String> roles;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private boolean approved;
    private Instant createdAt;
}
