package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageTag;

public class StorageTagRepository extends JPARepository<StorageTag> {
    public StorageTagRepository() {
        super(StorageTag.class);
    }
}
