package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.MonitoringTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MonitoringTaskRepository extends JpaRepository<MonitoringTask, UUID> {
}
