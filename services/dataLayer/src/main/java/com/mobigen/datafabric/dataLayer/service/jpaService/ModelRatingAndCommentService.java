package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.ModelRatingAndComment;
import dto.compositeKeys.ModelRatingAndCommentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelRatingAndCommentService extends SyncService<ModelRatingAndComment, ModelRatingAndCommentKey> {
    public ModelRatingAndCommentService(JpaRepository<ModelRatingAndComment, ModelRatingAndCommentKey> repository) {
        super(repository);
    }
}
