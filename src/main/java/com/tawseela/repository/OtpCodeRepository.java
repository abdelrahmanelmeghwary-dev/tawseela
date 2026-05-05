package com.tawseela.repository;

import com.tawseela.domain.OtpCode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    /** Most recent non-expired code for this phone. */
    @Query("SELECT o FROM OtpCode o WHERE o.phone = :phone AND o.expiresAt > :now ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpCode> findLatestValid(String phone, Instant now);

    /** Remove all codes for a phone (called after successful verify). */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.phone = :phone")
    void deleteAllByPhone(String phone);

    /** Scheduled cleanup — removes all expired rows. */
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt <= :now")
    int deleteExpired(Instant now);
}
