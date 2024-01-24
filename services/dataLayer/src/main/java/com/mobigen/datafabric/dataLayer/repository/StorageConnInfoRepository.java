package com.mobigen.datafabric.dataLayer.repository;

import dto.StorageConnInfo;

public class StorageConnInfoRepository extends JPARepository<StorageConnInfo> {
    public StorageConnInfoRepository() {
        super(StorageConnInfo.class);
    }
}
