package com.tawseela.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Unified payload for {@code POST /api/auth/otp/verify} — only one nested result applies per purpose. */
@Getter
@Setter
@NoArgsConstructor
public class OtpVerifyApiResponse {

    private String purpose;
    private RegisterVerifyResponse registration;
    private ForgotPasswordVerifyResponse forgotPassword;
    private Boolean loginOtpVerified;
}
