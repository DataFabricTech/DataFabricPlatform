package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.ModelMetadata;
import dto.compositeKeys.ModelMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelMetadataService extends JpaService<ModelMetadata, ModelMetadataKey> {
    public ModelMetadataService(JpaRepository<ModelMetadata, ModelMetadataKey> repository) {
        super(repository);
    }
}
