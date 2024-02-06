package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelFeedback;
import dto.compositeKeys.ModelFeedbackKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelFeedbackRepository extends JpaRepository<ModelFeedback, ModelFeedbackKey> {
}
