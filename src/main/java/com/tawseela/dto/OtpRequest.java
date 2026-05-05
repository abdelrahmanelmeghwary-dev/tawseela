package com.tawseela.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(@NotBlank String phone) {}
