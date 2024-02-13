package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.ModelFeedback;
import dto.compositeKeys.ModelFeedbackKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelFeedbackService extends SyncService<ModelFeedback, ModelFeedbackKey> {
    public ModelFeedbackService(JpaRepository<ModelFeedback, ModelFeedbackKey> repository) {
        super(repository);
    }
}
