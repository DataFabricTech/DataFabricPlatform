package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelRatingAndComment;
import dto.compositeKeys.ModelRatingAndCommentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRatingAndCommentRepository extends JpaRepository<ModelRatingAndComment, ModelRatingAndCommentKey> {
}
