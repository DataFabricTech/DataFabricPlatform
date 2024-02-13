package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.StorageMetadata;
import dto.compositeKeys.StorageMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageMetadataService extends SyncService<StorageMetadata, StorageMetadataKey> {
    public StorageMetadataService(JpaRepository<StorageMetadata, StorageMetadataKey> repository) {
        super(repository);
    }
}
