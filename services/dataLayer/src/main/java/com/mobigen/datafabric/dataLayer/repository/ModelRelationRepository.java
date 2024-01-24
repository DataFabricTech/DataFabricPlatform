package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelRelation;

public class ModelRelationRepository extends JPARepository<ModelRelation> {
    public ModelRelationRepository() {
        super(ModelRelation.class);
    }
}
