package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.StorageTag;
import dto.compositeKeys.StorageTagKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageTagService extends JpaService<StorageTag, StorageTagKey> {
    public StorageTagService(JpaRepository<StorageTag, StorageTagKey> repository) {
        super(repository);
    }
}
