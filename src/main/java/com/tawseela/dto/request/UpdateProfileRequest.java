package com.tawseela.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String fullName;
    private String fcmToken;
    private String avatarUrl;
}
