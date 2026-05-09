package com.tawseela.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterVerifyRequest {

    @NotBlank
    private String mobileNumber;

    @NotBlank
    private String otpCode;
}

