package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, String> {
    @Query(nativeQuery = true, value = "select metadata_value as metadataValue " +
            "from metadata " +
            "where metadata_name = 'recent_collected_time' " +
            "order by created_at DESC " +
            "LIMIT 1")
    public Optional<String> getRecentCollectedTime();

    @Query(nativeQuery = true, value = "select metadata_value as metadataValue from metadata where metadata_name like 'service.%' order by metadataValue")
    public List<String> findAllTypes();
}
