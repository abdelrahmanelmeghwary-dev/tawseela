package com.tawseela.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNotificationRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    private Map<String, String> data;
}
