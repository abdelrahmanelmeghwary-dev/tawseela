package com.tawseela.dto.request;

import com.tawseela.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

    @NotNull
    private OrderStatus newStatus;

    private String note;

    private UUID driverId;
}
