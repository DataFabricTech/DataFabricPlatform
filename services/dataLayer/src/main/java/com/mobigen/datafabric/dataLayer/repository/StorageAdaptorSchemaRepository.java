package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageAdaptorSchema;
import jakarta.persistence.EntityManager;

public class StorageAdaptorSchemaRepository extends JPARepository<StorageAdaptorSchema> {
    public StorageAdaptorSchemaRepository(EntityManager em) {
        super(StorageAdaptorSchema.class, em);
    }
}
