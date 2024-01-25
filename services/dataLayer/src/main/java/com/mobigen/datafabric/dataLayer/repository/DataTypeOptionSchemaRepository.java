package com.mobigen.datafabric.dataLayer.repository;

import dto.DataTypeOptionSchema;
import jakarta.persistence.EntityManager;

public class DataTypeOptionSchemaRepository extends JPARepository<DataTypeOptionSchema> {
    public DataTypeOptionSchemaRepository(EntityManager em) {
        super(DataTypeOptionSchema.class, em);
    }
}
