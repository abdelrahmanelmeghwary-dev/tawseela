package com.tawseela.dto;

import com.tawseela.util.PasswordRules;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordResetRequest {

    @NotBlank
    private String mobileNumber;

    @NotBlank
    private String resetToken;

    @NotBlank
    @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MESSAGE)
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}

