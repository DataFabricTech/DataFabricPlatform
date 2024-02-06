package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.StorageMetadata;
import dto.compositeKeys.StorageMetadataKey;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageMetadataRepository extends JpaRepository<StorageMetadata, StorageMetadataKey> {
}
