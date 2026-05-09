package com.tawseela.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordSendOtpRequest {

    @NotBlank
    private String mobileNumber;
}

