package com.tawseela.dto.response;

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
    private AuthMeResponse user;
}