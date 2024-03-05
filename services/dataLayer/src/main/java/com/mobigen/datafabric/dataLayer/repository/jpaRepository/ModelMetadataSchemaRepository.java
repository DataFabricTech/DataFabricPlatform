package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelMetadataSchema;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModelMetadataSchemaRepository extends JpaRepository<ModelMetadataSchema, UUID> {
    List<ModelMetadataSchema> findByName(String name, Pageable pageable);
}
