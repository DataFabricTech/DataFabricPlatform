package com.mobigen.datafabric.dataLayer.repository;

import dto.DataTypeOption;
import jakarta.persistence.EntityManager;

public class DataTypeOptionRepository extends JPARepository<DataTypeOption> {
    public DataTypeOptionRepository(EntityManager em) {
        super(DataTypeOption.class, em);
    }
}
