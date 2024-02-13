package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.ModelTag;
import dto.compositeKeys.ModelTagKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ModelTagService extends SyncService<ModelTag, ModelTagKey> {
    public ModelTagService(JpaRepository<ModelTag, ModelTagKey> repository) {
        super(repository);
    }
}
