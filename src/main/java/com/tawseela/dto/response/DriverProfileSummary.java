package com.tawseela.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriverProfileSummary {
    UUID id;
    String fullName;
    String phone;
}
