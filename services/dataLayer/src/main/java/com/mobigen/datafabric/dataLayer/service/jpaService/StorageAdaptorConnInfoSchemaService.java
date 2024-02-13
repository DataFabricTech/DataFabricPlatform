package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.StorageAdaptorConnInfoSchema;
import dto.compositeKeys.StorageAdaptorConnInfoSchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class StorageAdaptorConnInfoSchemaService extends SyncService<StorageAdaptorConnInfoSchema, StorageAdaptorConnInfoSchemaKey> {
    public StorageAdaptorConnInfoSchemaService(JpaRepository<StorageAdaptorConnInfoSchema, StorageAdaptorConnInfoSchemaKey> repository) {
        super(repository);
    }
}
