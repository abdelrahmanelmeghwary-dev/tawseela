package com.tawseela.controller;

import com.tawseela.dto.ApiResponse;
import com.tawseela.dto.AuthTokensResponse.UserInfoDto;
import com.tawseela.mapper.UserMapper;
import com.tawseela.security.TawseelaUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final UserMapper userMapper;

    public CustomerController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDto>> me(@AuthenticationPrincipal TawseelaUserDetails principal) {
        UserInfoDto dto = userMapper.toUserInfo(principal.getUser());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
