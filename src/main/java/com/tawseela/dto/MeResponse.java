package com.tawseela.dto;

import java.time.Instant;
import java.util.UUID;

public record MeResponse(
        UUID id,
        String phone,
        String email,
        String fullName,
        String role,
        String fcmToken,
        Instant createdAt,
        Instant updatedAt) {}
