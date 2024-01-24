package com.mobigen.datafabric.dataLayer.repository;

import dto.DataTypeOption;

public class DataTypeOptionRepository extends JPARepository<DataTypeOption> {
    public DataTypeOptionRepository() {
        super(DataTypeOption.class);
    }
}
