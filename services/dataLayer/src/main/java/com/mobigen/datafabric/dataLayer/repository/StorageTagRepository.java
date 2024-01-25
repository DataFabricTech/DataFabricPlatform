package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageTag;
import jakarta.persistence.EntityManager;

public class StorageTagRepository extends JPARepository<StorageTag> {
    public StorageTagRepository(EntityManager em) {
        super(StorageTag.class, em);
    }
}
