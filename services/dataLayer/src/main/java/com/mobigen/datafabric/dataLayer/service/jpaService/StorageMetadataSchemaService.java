package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.repository.jpaRepository.StorageMetadataSchemaRepository;
import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.StorageMetadataSchema;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StorageMetadataSchemaService extends SyncService<StorageMetadataSchema, UUID> {

    private final StorageMetadataSchemaRepository storageMetadataSchemaRepository;

    public StorageMetadataSchemaService(JpaRepository<StorageMetadataSchema, UUID> repository, StorageMetadataSchemaRepository storageMetadataSchemaRepository) {
        super(repository);
        this.storageMetadataSchemaRepository = storageMetadataSchemaRepository;
    }

    public List<StorageMetadataSchema> findByName(String name, Pageable pageable) {
        return storageMetadataSchemaRepository.findByName(name, pageable);
    }
}
