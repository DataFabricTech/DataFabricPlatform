package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.StorageMetadataSchema;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StorageMetadataSchemaRepository extends JpaRepository<StorageMetadataSchema, UUID> {
    List<StorageMetadataSchema> findByName(String name, Pageable pageable);
}
