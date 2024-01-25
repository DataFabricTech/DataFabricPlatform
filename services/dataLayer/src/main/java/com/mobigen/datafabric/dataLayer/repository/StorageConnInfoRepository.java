package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageConnInfo;
import jakarta.persistence.EntityManager;

public class StorageConnInfoRepository extends JPARepository<StorageConnInfo> {
    public StorageConnInfoRepository(EntityManager em) {
        super(StorageConnInfo.class, em);
    }
}
