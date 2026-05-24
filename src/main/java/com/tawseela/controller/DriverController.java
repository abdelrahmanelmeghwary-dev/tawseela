package com.tawseela.controller;

import com.tawseela.dto.response.DriverResponse;
import com.tawseela.dto.request.UpdateDriverRequest;
import com.tawseela.service.DriverService;
import com.tawseela.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drivers")
@Tag(name = "Drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','DRIVER','ADMIN')")
    @Operation(summary = "List drivers")
    public ResponseEntity<ApiResponse<Page<DriverResponse>>> list(
            @RequestParam(required = false) Boolean isOnline,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.list(isOnline, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','DRIVER','ADMIN')")
    @Operation(
            summary = "Get driver by user id",
            description =
                    "Path id is the user's UUID (userId from admin driver list), not driverProfileId. "
                            + "Runtime row is created automatically for approved drivers.")
    public ResponseEntity<ApiResponse<DriverResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Create driver runtime profile")
    public ResponseEntity<ApiResponse<DriverResponse>> create() {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(driverService.create()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update driver runtime profile")
    public ResponseEntity<ApiResponse<DriverResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateDriverRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.update(id, request)));
    }
}
