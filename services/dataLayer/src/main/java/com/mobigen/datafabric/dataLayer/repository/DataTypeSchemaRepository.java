package com.mobigen.datafabric.dataLayer.repository;

import dto.DataTypeSchema;
import jakarta.persistence.EntityManager;

public class DataTypeSchemaRepository extends JPARepository<DataTypeSchema> {
    public DataTypeSchemaRepository(EntityManager em) {
        super(DataTypeSchema.class, em);
    }
}
