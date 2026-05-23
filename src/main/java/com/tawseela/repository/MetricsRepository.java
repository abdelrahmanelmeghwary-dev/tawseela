package com.tawseela.repository;

import com.tawseela.entity.Order;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MetricsRepository extends JpaRepository<Order, UUID> {

    @Query(
            value =
                    """
            SELECT
              COUNT(*) FILTER (WHERE DATE(created_at) = :targetDate),
              COUNT(*) FILTER (WHERE status = 'COMPLETED' AND DATE(created_at) = :targetDate),
              COUNT(*) FILTER (WHERE status = 'CANCELLED' AND DATE(created_at) = :targetDate)
            FROM orders
            """,
            nativeQuery = true)
    List<Object[]> countOrderMetrics(@Param("targetDate") LocalDate targetDate);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.online = true")
    long countOnlineDrivers();
}
