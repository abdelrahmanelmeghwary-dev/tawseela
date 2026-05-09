package com.tawseela.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRowDto {

    private UUID id;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean phoneVerified;
    private List<String> roles;
    private Instant createdAt;
}
