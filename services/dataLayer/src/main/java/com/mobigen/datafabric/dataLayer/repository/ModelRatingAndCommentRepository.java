package com.mobigen.datafabric.dataLayer.repository;

import dto.ModelRatingAndComment;

public class ModelRatingAndCommentRepository extends JPARepository<ModelRatingAndComment> {
    public ModelRatingAndCommentRepository() {
        super(ModelRatingAndComment.class);
    }
}
