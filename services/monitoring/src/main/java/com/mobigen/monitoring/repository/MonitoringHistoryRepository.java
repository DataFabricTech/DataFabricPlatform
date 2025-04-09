package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.MonitoringHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MonitoringHistoryRepository extends JpaRepository<MonitoringHistory, UUID> {
}
