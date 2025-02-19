package com.mobigen.monitoring.repository;

import com.mobigen.monitoring.domain.Ingestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngestionsRepository extends JpaRepository<Ingestion, UUID> {
}
