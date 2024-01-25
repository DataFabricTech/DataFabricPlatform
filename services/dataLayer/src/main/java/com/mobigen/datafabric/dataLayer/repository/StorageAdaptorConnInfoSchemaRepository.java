package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageAdaptorConnInfoSchema;
import jakarta.persistence.EntityManager;

public class StorageAdaptorConnInfoSchemaRepository extends JPARepository<StorageAdaptorConnInfoSchema> {
    public StorageAdaptorConnInfoSchemaRepository(EntityManager em) {
        super(StorageAdaptorConnInfoSchema.class, em);
    }
}
