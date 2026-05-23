package com.tawseela.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordVerifyOtpRequest {

    @NotBlank
    private String mobileNumber;

    @NotBlank
    private String otpCode;
}

