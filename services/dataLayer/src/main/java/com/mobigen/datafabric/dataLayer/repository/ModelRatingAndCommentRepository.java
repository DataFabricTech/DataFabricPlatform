package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelRatingAndComment;
import jakarta.persistence.EntityManager;

public class ModelRatingAndCommentRepository extends JPARepository<ModelRatingAndComment> {
    public ModelRatingAndCommentRepository(EntityManager em) {
        super(ModelRatingAndComment.class, em);
    }
}
