package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelRelation;
import jakarta.persistence.EntityManager;

public class ModelRelationRepository extends JPARepository<ModelRelation> {
    public ModelRelationRepository(EntityManager em) {
        super(ModelRelation.class, em);
    }
}
