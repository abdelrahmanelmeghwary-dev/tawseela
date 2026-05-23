package com.tawseela.controller;

import com.tawseela.common.ApiResponse;
import com.tawseela.dto.request.DriverProfileRequest;
import com.tawseela.dto.request.ForgotPasswordResetRequest;
import com.tawseela.dto.request.ForgotPasswordSendOtpRequest;
import com.tawseela.dto.request.ForgotPasswordVerifyOtpRequest;
import com.tawseela.dto.response.AuthMeResponse;
import com.tawseela.dto.response.AuthTokensResponse;
import com.tawseela.dto.response.ForgotPasswordVerifyResponse;
import com.tawseela.dto.request.LoginRequest;
import com.tawseela.dto.request.LogoutRequest;
import com.tawseela.dto.request.OtpSendPublicRequest;
import com.tawseela.dto.response.OtpVerifyApiResponse;
import com.tawseela.dto.request.OtpVerifyPublicRequest;
import com.tawseela.dto.request.RefreshTokenRequest;
import com.tawseela.dto.request.RegisterRequest;
import com.tawseela.dto.request.RegisterVerifyRequest;
import com.tawseela.dto.response.RegisterVerifyResponse;
import com.tawseela.security.TawseelaUserDetails;
import com.tawseela.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Current authenticated user")
    public ResponseEntity<ApiResponse<AuthMeResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentUser()));
    }

    @PatchMapping("/me/driver-profile")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update driver vehicle profile")
    public ResponseEntity<ApiResponse<AuthMeResponse>> updateDriverProfile(
            @Valid @RequestBody DriverProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.updateDriverProfile(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful. OTP sent to your mobile.", null));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<RegisterVerifyResponse>> verifyRegister(
            @Valid @RequestBody RegisterVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.verifyRegistration(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request.getRefreshToken())));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Void>> otpSend(@Valid @RequestBody OtpSendPublicRequest request) {
        authService.sendOtpPublic(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent", null));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<OtpVerifyApiResponse>> otpVerify(@Valid @RequestBody OtpVerifyPublicRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.verifyOtpPublic(request)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            @AuthenticationPrincipal TawseelaUserDetails principal,
            @Valid @RequestBody LogoutRequest request) {
        UUID userId = principal.getUser().getId();
        authService.logout(userId, request.getRefreshToken(), httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<Void>> forgotSend(@Valid @RequestBody ForgotPasswordSendOtpRequest request) {
        authService.forgotSendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent", null));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse<ForgotPasswordVerifyResponse>> forgotVerify(
            @Valid @RequestBody ForgotPasswordVerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.forgotVerifyOtp(request)));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> forgotReset(@Valid @RequestBody ForgotPasswordResetRequest request) {
        authService.forgotReset(request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated", null));
    }
}
