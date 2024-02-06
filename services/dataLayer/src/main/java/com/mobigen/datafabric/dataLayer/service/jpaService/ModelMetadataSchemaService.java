package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.ModelMetadataSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ModelMetadataSchemaService extends JpaService<ModelMetadataSchema, UUID> {
    public ModelMetadataSchemaService(JpaRepository<ModelMetadataSchema, UUID> repository) {
        super(repository);
    }
}
