package com.mobigen.datafabric.dataLayer.repository;

import dto.Model;

public class ModelRepository extends JPARepository<Model> {
    public ModelRepository() {
        super(Model.class);
    }
}
