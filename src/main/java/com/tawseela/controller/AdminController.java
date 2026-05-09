package com.tawseela.controller;

import com.tawseela.admin.AdminService;
import com.tawseela.dto.AdminDriverRowDto;
import com.tawseela.dto.AdminUserRowDto;
import com.tawseela.dto.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserRowDto>>> users() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listUsers()));
    }

    @GetMapping("/drivers")
    public ResponseEntity<ApiResponse<List<AdminDriverRowDto>>> drivers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listDrivers()));
    }

    @PutMapping("/drivers/{id}/approve")
    public ResponseEntity<ApiResponse<AdminDriverRowDto>> approve(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.approveDriver(id)));
    }

    @PutMapping("/drivers/{id}/reject")
    public ResponseEntity<ApiResponse<AdminDriverRowDto>> reject(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.rejectDriver(id)));
    }
}
