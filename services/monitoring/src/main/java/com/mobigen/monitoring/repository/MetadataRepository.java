package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, UUID> {
    @Query(nativeQuery = true, value = "" +
            "select metadata_value as metadataValue " +
            "from metadata " +
            "where metadata_name = 'recent_collected_time'")
    String getRecentCollectedTime();
}
