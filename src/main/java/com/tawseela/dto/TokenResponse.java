package com.tawseela.dto;

import java.util.UUID;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserSummary user) {

    public record UserSummary(UUID id, String phone, String email, String fullName, String role) {}
}
