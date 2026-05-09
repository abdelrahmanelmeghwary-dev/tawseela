package com.tawseela.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokensResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresInSeconds;
    private UserInfoDto user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto {
        private UUID userId;
        private String mobileNumber;
        private String firstName;
        private String lastName;
        private List<String> roles;
    }
}
