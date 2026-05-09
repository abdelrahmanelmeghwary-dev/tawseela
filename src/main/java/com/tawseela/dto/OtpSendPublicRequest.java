package com.tawseela.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpSendPublicRequest {

    @NotBlank
    private String mobileNumber;

    @NotBlank
    @Pattern(regexp = "REGISTER|LOGIN|FORGET_PASSWORD", message = "purpose must be REGISTER, LOGIN, or FORGET_PASSWORD")
    private String purpose;
}

