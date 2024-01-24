package com.mobigen.datafabric.dataLayer.repository;

import dto.DataTypeSchema;

public class DataTypeSchemaRepository extends JPARepository<DataTypeSchema> {
    public DataTypeSchemaRepository() {
        super(DataTypeSchema.class);
    }
}
