package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.IngestionHistory;
import com.mobigen.monitoring.vo.IngestionHistoryVo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngestionHistoryRepository extends JpaRepository<IngestionHistory, UUID> {
    @Query(nativeQuery = true, value = "" +
            "select ih.event_at as eventAt, i.ingestion_name as ingestionName, i.type, ih.event, " +
            "ih.state, i.service_id as serviceId, s.service_name as serviceName, s.service_display_name as serviceDisplayName, " +
            "s.service_type as dbType " +
            "from ingestion_history as ih " +
            "left join ingestion as i on ih.ingestion_id = i.ingestion_id " +
            "left join services as s on i.ingestion_id = s.service_id")
    public List<IngestionHistoryVo> findIngestionHistoryResponse(PageRequest pageRequest);
}
