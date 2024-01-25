package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelFeedback;
import jakarta.persistence.EntityManager;

public class ModelFeedbackRepository extends JPARepository<ModelFeedback> {
    public ModelFeedbackRepository(EntityManager em) {
        super(ModelFeedback.class, em);
    }
}
