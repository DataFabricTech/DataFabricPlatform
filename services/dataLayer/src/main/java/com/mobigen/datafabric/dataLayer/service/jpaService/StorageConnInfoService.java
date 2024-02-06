package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.StorageConnInfo;
import dto.compositeKeys.StorageConnInfoKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageConnInfoService extends JpaService<StorageConnInfo, StorageConnInfoKey> {
    public StorageConnInfoService(JpaRepository<StorageConnInfo, StorageConnInfoKey> repository) {
        super(repository);
    }
}
