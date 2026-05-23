package com.tawseela.dto.request;

import com.tawseela.util.PasswordRules;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    private String mobileNumber;

    @NotBlank
    @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MESSAGE)
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    @Pattern(regexp = "CUSTOMER|DRIVER", message = "role must be CUSTOMER or DRIVER")
    private String role;

    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
}

