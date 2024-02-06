package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.ModelRelation;
import dto.compositeKeys.ModelRelationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelRelationService extends JpaService<ModelRelation, ModelRelationKey> {
    public ModelRelationService(JpaRepository<ModelRelation, ModelRelationKey> repository) {
        super(repository);
    }
}
