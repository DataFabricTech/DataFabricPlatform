package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.SlowQueryStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SlowQueryStatisticRepository extends JpaRepository<SlowQueryStatistic, UUID> {
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM slow_query_statistic e WHERE e.created_at > ?1")
    public void deleteOlderThan(long threshold);
}
