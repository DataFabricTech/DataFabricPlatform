package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.MonitoringLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MonitoringTaskRepository extends JpaRepository<MonitoringLog, UUID> {
}
