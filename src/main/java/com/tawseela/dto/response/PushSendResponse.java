package com.tawseela.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PushSendResponse {
    boolean success;
    String reason;
}
