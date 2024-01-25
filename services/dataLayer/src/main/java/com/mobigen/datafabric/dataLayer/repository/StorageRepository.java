package com.mobigen.datafabric.dataLayer.repository;

import dto.Storage;
import jakarta.persistence.EntityManager;

public class StorageRepository extends JPARepository<Storage> {
    public StorageRepository(EntityManager em) {
        super(Storage.class, em);
    }
}
