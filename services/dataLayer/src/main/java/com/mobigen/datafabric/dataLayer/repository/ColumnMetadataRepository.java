package com.mobigen.datafabric.dataLayer.repository;

import dto.ColumnMetadata;

public class ColumnMetadataRepository extends JPARepository<ColumnMetadata> {
    public ColumnMetadataRepository() {
        super(ColumnMetadata.class);
    }
}
