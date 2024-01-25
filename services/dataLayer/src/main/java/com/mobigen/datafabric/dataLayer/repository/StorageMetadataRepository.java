package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageMetadata;
import jakarta.persistence.EntityManager;

public class StorageMetadataRepository extends JPARepository<StorageMetadata> {
    public StorageMetadataRepository(EntityManager em) {
        super(StorageMetadata.class, em);
    }
}
