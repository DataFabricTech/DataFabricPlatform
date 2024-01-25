package com.mobigen.datafabric.dataLayer.repository;

import dto.DataAutoAdd;
import jakarta.persistence.EntityManager;

public class DataAutoAddRepository extends JPARepository<DataAutoAdd> {
    public DataAutoAddRepository(EntityManager em) {
        super(DataAutoAdd.class, em);
    }
}
