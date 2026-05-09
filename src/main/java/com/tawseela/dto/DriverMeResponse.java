package com.tawseela.dto;

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
public class DriverMeResponse {

    private UUID userId;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private UUID driverProfileId;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private boolean approved;
}
