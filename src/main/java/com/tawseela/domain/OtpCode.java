package com.tawseela.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_codes")
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String phone;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public OtpCode() {}

    public OtpCode(String phone, String code, Instant expiresAt) {
        this.phone = phone;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }

    public String getPhone() { return phone; }

    public String getCode() { return code; }

    public Instant getExpiresAt() { return expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
}
