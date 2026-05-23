package com.tawseela.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal deliveryLat;

    @NotNull
    private BigDecimal deliveryLng;

    private String deliveryAddress;
}
