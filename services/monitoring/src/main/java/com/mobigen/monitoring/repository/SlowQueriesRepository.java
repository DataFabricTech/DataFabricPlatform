package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.SlowQueries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlowQueriesRepository extends JpaRepository<SlowQueries, UUID> {
}
