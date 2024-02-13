package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.StorageTag;
import dto.compositeKeys.StorageTagKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageTagService extends SyncService<StorageTag, StorageTagKey> {
    public StorageTagService(JpaRepository<StorageTag, StorageTagKey> repository) {
        super(repository);
    }
}
