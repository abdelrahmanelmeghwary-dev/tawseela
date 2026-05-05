package com.tawseela.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param role Optional, only for new users: {@code customer} (default) or {@code driver}.
 *             Ignored when the phone already exists. {@code admin} is not allowed here; set admin in DB or via an admin-only API.
 * @param fullName Optional; stored on verify (new or returning user).
 * @param email Optional; if omitted for a new user, a synthetic address {@code {digits}@phone.tawseela.local} is stored (same idea as Supabase test phone flow).
 * @param fcmToken Optional FCM device token.
 */
public record OtpVerifyRequest(
        @NotBlank String phone,
        @NotBlank String otp,
        String role,
        @Size(max = 255) String fullName,
        @Email String email,
        @Size(max = 512) String fcmToken) {

    public OtpVerifyRequest(String phone, String otp) {
        this(phone, otp, null, null, null, null);
    }

    public OtpVerifyRequest(String phone, String otp, String role) {
        this(phone, otp, role, null, null, null);
    }
}
