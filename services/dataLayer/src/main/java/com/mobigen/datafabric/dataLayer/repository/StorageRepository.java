package com.mobigen.datafabric.dataLayer.repository;

import dto.Storage;

public class StorageRepository extends JPARepository<Storage> {
    public StorageRepository() {
        super(Storage.class);
    }
}
