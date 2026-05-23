package com.tawseela.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProfileResponse {
    UUID id;
    String phone;
    String fullName;
    String fcmToken;
    String avatarUrl;
    List<String> roles;
    Instant createdAt;
    Instant updatedAt;
}
