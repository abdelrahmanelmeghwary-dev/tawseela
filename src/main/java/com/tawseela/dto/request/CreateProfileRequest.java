package com.tawseela.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProfileRequest {

    private UUID id;

    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;
}
