package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageAdaptorSchema;

public class StorageAdaptorSchemaRepository extends JPARepository<StorageAdaptorSchema> {
    public StorageAdaptorSchemaRepository() {
        super(StorageAdaptorSchema.class);
    }
}
