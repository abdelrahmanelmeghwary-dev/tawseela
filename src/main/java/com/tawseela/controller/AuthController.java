package com.tawseela.controller;

import com.tawseela.dto.MeResponse;
import com.tawseela.dto.OtpRequest;
import com.tawseela.dto.OtpVerifyRequest;
import com.tawseela.dto.RefreshTokenRequest;
import com.tawseela.dto.TokenResponse;
import com.tawseela.security.JwtPrincipal;
import com.tawseela.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/otp/request")
    public Map<String, String> requestOtp(@Valid @RequestBody OtpRequest body) {
        authService.requestOtp(body.phone());
        return Map.of("message", "OTP sent successfully");
    }

    @PostMapping("/otp/verify")
    public TokenResponse verify(@Valid @RequestBody OtpVerifyRequest body) {
        return authService.verifyOtp(body);
    }

    @PostMapping("/token/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest body) {
        return authService.refresh(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return authService.me(principal);
    }
}
