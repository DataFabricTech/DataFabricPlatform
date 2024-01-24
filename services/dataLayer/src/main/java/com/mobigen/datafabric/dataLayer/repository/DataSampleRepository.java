package com.mobigen.datafabric.dataLayer.repository;

import dto.DataSample;

public class DataSampleRepository extends JPARepository<DataSample> {
    public DataSampleRepository() {
        super(DataSample.class);
    }
}
