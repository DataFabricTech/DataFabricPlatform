package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.StorageMetadataSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StorageMetadataSchemaService extends JpaService<StorageMetadataSchema, UUID> {
    public StorageMetadataSchemaService(JpaRepository<StorageMetadataSchema, UUID> repository) {
        super(repository);
    }
}
