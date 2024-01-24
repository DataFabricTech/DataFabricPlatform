package com.mobigen.datafabric.dataLayer.repository;

import dto.DataTypeOptionSchema;

public class DataTypeOptionSchemaRepository extends JPARepository<DataTypeOptionSchema> {
    public DataTypeOptionSchemaRepository() {
        super(DataTypeOptionSchema.class);
    }
}
