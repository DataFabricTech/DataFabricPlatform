package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelMetadataSchema;
import jakarta.persistence.EntityManager;

public class ModelMetadataSchemaRepository extends JPARepository<ModelMetadataSchema> {
    public ModelMetadataSchemaRepository(EntityManager em) {
        super(ModelMetadataSchema.class, em);
    }
}
