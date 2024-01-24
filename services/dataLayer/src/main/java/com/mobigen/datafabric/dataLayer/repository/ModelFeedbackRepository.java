package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelFeedback;

public class ModelFeedbackRepository extends JPARepository<ModelFeedback> {
    public ModelFeedbackRepository() {
        super(ModelFeedback.class);
    }
}
