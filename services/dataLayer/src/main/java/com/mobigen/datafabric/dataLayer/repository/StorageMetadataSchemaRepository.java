package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageMetadataSchema;

public class StorageMetadataSchemaRepository extends JPARepository<StorageMetadataSchema> {
    public StorageMetadataSchemaRepository() {
        super(StorageMetadataSchema.class);
    }
}
