package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelMetadata;
import dto.compositeKeys.ModelMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelMetadataRepository extends JpaRepository<ModelMetadata, ModelMetadataKey> {
}
