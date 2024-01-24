package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelMetadata;

public class ModelMetadataRepository extends JPARepository<ModelMetadata> {
    public ModelMetadataRepository() {
        super(ModelMetadata.class);
    }
}
