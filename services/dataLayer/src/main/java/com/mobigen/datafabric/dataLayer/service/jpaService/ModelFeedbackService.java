package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.ModelFeedback;
import dto.compositeKeys.ModelFeedbackKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelFeedbackService extends JpaService<ModelFeedback, ModelFeedbackKey> {
    public ModelFeedbackService(JpaRepository<ModelFeedback, ModelFeedbackKey> repository) {
        super(repository);
    }
}
