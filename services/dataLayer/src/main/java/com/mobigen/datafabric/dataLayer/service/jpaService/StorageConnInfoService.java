package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.StorageConnInfo;
import dto.compositeKeys.StorageConnInfoKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageConnInfoService extends SyncService<StorageConnInfo, StorageConnInfoKey> {
    public StorageConnInfoService(JpaRepository<StorageConnInfo, StorageConnInfoKey> repository) {
        super(repository);
    }
}
