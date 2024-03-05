package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.repository.jpaRepository.ModelMetadataSchemaRepository;
import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.ModelMetadataSchema;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ModelMetadataSchemaService extends SyncService<ModelMetadataSchema, UUID> {
    private final ModelMetadataSchemaRepository modelMetadataSchemaRepository;
    public ModelMetadataSchemaService(JpaRepository<ModelMetadataSchema, UUID> repository, ModelMetadataSchemaRepository modelMetadataSchemaRepository) {
        super(repository);
        this.modelMetadataSchemaRepository = modelMetadataSchemaRepository;
    }

    public List<ModelMetadataSchema> findByName(String name, Pageable pageable) {
        return modelMetadataSchemaRepository.findByName(name, pageable);
    }
}
