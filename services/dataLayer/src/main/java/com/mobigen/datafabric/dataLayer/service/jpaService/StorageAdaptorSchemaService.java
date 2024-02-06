package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.StorageAdaptorSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StorageAdaptorSchemaService extends JpaService<StorageAdaptorSchema, UUID> {
    public StorageAdaptorSchemaService(JpaRepository<StorageAdaptorSchema, UUID> repository) {
        super(repository);
    }
}
