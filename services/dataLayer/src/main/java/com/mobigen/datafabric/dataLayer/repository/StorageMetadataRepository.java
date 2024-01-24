package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageMetadata;

public class StorageMetadataRepository extends JPARepository<StorageMetadata> {
    public StorageMetadataRepository() {
        super(StorageMetadata.class);
    }
}
