package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelMetadata;
import jakarta.persistence.EntityManager;

public class ModelMetadataRepository extends JPARepository<ModelMetadata> {
    public ModelMetadataRepository(EntityManager em) {
        super(ModelMetadata.class, em);
    }
}
