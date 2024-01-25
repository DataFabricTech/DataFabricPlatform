package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageMetadataSchema;
import jakarta.persistence.EntityManager;

public class StorageMetadataSchemaRepository extends JPARepository<StorageMetadataSchema> {
    public StorageMetadataSchemaRepository(EntityManager em) {
        super(StorageMetadataSchema.class, em);
    }
}
