package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelMetadataSchema;

public class ModelMetadataSchemaRepository extends JPARepository<ModelMetadataSchema> {
    public ModelMetadataSchemaRepository() {
        super(ModelMetadataSchema.class);
    }
}
