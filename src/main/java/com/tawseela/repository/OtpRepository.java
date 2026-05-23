package com.tawseela.repository;

import com.tawseela.entity.OtpEntity;
import com.tawseela.enums.OtpPurpose;
import com.tawseela.enums.OtpStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {

    Optional<OtpEntity> findByIdAndUser_Id(UUID id, UUID userId);

    @Query(
            "SELECT o FROM OtpEntity o WHERE o.user.id = :userId AND o.purpose = :purpose AND o.status = :status ORDER BY o.createdAt DESC")
    List<OtpEntity> findLatestForUserPurposeStatus(
            @Param("userId") UUID userId,
            @Param("purpose") OtpPurpose purpose,
            @Param("status") OtpStatus status);

    default Optional<OtpEntity> findLatestPending(UUID userId, OtpPurpose purpose) {
        List<OtpEntity> list = findLatestForUserPurposeStatus(userId, purpose, OtpStatus.PENDING);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "UPDATE OtpEntity o SET o.status = :expired WHERE o.status = :pending AND o.expiresAt < :now")
    int expirePendingPastDue(
            @Param("expired") OtpStatus expired, @Param("pending") OtpStatus pending, @Param("now") Instant now);
}
