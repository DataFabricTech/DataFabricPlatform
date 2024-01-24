package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageAdaptorConnInfoSchema;

public class StorageAdaptorConnInfoSchemaRepository extends JPARepository<StorageAdaptorConnInfoSchema> {
    public StorageAdaptorConnInfoSchemaRepository() {
        super(StorageAdaptorConnInfoSchema.class);
    }
}
