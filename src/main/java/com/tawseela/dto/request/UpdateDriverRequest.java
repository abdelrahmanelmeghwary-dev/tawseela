package com.tawseela.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDriverRequest {
    private Boolean online;
    private Instant lastSeen;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
}
