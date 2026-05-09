package com.tawseela.controller;

import com.tawseela.dto.ApiResponse;
import com.tawseela.dto.DriverMeResponse;
import com.tawseela.dto.DriverProfileRequest;
import com.tawseela.driver.DriverService;
import com.tawseela.security.TawseelaUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('DRIVER')")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<DriverMeResponse>> upsertProfile(
            @AuthenticationPrincipal TawseelaUserDetails principal,
            @Valid @RequestBody DriverProfileRequest request) {
        DriverMeResponse body = driverService.upsertProfile(principal.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DriverMeResponse>> me(@AuthenticationPrincipal TawseelaUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.me(principal.getUser().getId())));
    }
}

