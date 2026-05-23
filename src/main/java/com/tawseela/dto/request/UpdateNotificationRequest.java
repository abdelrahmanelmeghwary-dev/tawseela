package com.tawseela.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNotificationRequest {

    @NotNull
    private Boolean isRead;
}
