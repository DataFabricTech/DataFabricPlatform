package com.mobigen.datafabric.dataLayer.repository;

import dto.DataAutoAdd;

public class DataAutoAddRepository extends JPARepository<DataAutoAdd> {
    public DataAutoAddRepository() {
        super(DataAutoAdd.class);
    }
}
