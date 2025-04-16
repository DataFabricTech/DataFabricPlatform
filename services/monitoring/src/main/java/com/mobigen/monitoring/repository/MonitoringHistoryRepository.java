package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.MonitoringHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface MonitoringHistoryRepository extends JpaRepository<MonitoringHistory, UUID> {
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM monitoring_history e WHERE e.created_at > ?1")
    public void deleteOlderThan(long threshold);
}
