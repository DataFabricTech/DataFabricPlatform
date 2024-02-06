package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.ModelTag;
import dto.compositeKeys.ModelTagKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelTagService extends JpaService<ModelTag, ModelTagKey> {
    public ModelTagService(JpaRepository<ModelTag, ModelTagKey> repository) {
        super(repository);
    }
}
