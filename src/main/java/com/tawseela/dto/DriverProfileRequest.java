package com.tawseela.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverProfileRequest {

    @NotBlank
    @Size(max = 100)
    private String vehicleType;

    @NotBlank
    @Size(max = 100)
    private String vehicleNumber;

    @NotBlank
    @Size(max = 100)
    private String licenseNumber;
}

