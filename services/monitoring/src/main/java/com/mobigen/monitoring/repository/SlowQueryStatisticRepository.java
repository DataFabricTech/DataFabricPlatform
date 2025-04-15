package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.SlowQueryStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlowQueryStatisticRepository extends JpaRepository<SlowQueryStatistic, UUID> {
}
