package com.mobigen.datafabric.dataLayer.repository;

import dto.Model;
import jakarta.persistence.EntityManager;

public class ModelRepository extends JPARepository<Model> {
    public ModelRepository(EntityManager em) {
        super(Model.class, em);
    }
}
