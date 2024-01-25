package com.mobigen.datafabric.dataLayer.repository;

import dto.ColumnMetadata;
import jakarta.persistence.EntityManager;

public class ColumnMetadataRepository extends JPARepository<ColumnMetadata> {
    public ColumnMetadataRepository(EntityManager em) {
        super(ColumnMetadata.class, em);
    }
}
