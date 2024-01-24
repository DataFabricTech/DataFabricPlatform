package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelTag;

public class ModelTagRepository extends JPARepository<ModelTag> {
    public ModelTagRepository() {
        super(ModelTag.class);
    }
}
