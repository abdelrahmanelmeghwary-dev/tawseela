package com.tawseela.service;

import com.tawseela.dto.request.DriverProfileRequest;
import com.tawseela.dto.request.ForgotPasswordResetRequest;
import com.tawseela.dto.request.ForgotPasswordSendOtpRequest;
import com.tawseela.dto.request.ForgotPasswordVerifyOtpRequest;
import com.tawseela.dto.request.LoginRequest;
import com.tawseela.dto.request.RegisterRequest;
import com.tawseela.dto.request.RegisterVerifyRequest;
import com.tawseela.dto.response.AuthMeResponse;
import com.tawseela.dto.response.AuthTokensResponse;
import com.tawseela.dto.response.ForgotPasswordVerifyResponse;
import com.tawseela.dto.response.RegisterVerifyResponse;
import java.util.UUID;

public interface AuthService {

    void register(RegisterRequest request);

    RegisterVerifyResponse verifyRegistration(RegisterVerifyRequest request);

    AuthTokensResponse login(LoginRequest request);

    AuthTokensResponse refresh(String refreshToken);

    void logout(UUID userId, String refreshToken, String authorizationHeader);

    void forgotSendOtp(ForgotPasswordSendOtpRequest request);

    ForgotPasswordVerifyResponse forgotVerifyOtp(ForgotPasswordVerifyOtpRequest request);

    void forgotReset(ForgotPasswordResetRequest request);

    AuthMeResponse getCurrentUser();

    AuthMeResponse updateDriverProfile(DriverProfileRequest request);
}
