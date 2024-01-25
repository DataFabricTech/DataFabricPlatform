package com.mobigen.datafabric.dataLayer.repository;

import dto.DataSample;
import jakarta.persistence.EntityManager;

public class DataSampleRepository extends JPARepository<DataSample> {
    public DataSampleRepository(EntityManager em) {
        super(DataSample.class, em);
    }
}
