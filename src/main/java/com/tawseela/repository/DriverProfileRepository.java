package com.tawseela.repository;

import com.tawseela.entity.DriverProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {

    Optional<DriverProfile> findByUser_Id(UUID userId);

    @Query(
            "SELECT DISTINCT d FROM DriverProfile d JOIN FETCH d.user u JOIN FETCH u.roles WHERE d.id = :id")
    Optional<DriverProfile> findByIdWithUser(@Param("id") UUID id);

    @Query("SELECT DISTINCT d FROM DriverProfile d JOIN FETCH d.user u JOIN FETCH u.roles")
    List<DriverProfile> findAllWithUser();
}
