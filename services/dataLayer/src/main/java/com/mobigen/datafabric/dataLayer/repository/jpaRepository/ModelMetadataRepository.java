package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelMetadata;
import dto.compositeKeys.ModelMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModelMetadataRepository extends JpaRepository<ModelMetadata, ModelMetadataKey> {
    List<ModelMetadata> findByMetadataValue(String value);
}
