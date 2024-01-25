package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelTag;
import jakarta.persistence.EntityManager;

public class ModelTagRepository extends JPARepository<ModelTag> {
    public ModelTagRepository(EntityManager em) {
        super(ModelTag.class, em);
    }
}
