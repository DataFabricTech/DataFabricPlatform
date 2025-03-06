package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.TableAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TableAuditLogRepository extends JpaRepository<TableAuditLog, UUID> {
}
