package com.tawseela.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriverSummary {
    UUID id;
    String name;
    String mobileNumber;
}
