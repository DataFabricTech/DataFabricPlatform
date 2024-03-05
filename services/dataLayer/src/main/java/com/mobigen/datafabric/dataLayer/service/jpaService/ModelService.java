package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.repository.jpaRepository.*;
import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.Model;
import dto.enums.FormatType;
import dto.enums.StatusType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ModelService extends SyncService<Model, UUID> {
    private final ModelRepository modelRepository;
    private final ModelMetadataRepository modelMetadataRepository;
    private final ColumnMetadataRepository columnMetadataRepository;
    private final TagRepository tagRepository;
    private final ModelRelationRepository modelRelationRepository;

    public ModelService(JpaRepository<Model, UUID> repository, ModelRepository modelRepository, ModelMetadataRepository modelMetadataRepository, ColumnMetadataRepository columnMetadataRepository, TagRepository tagRepository, ModelTagRepository modelTagRepository, ModelRelationRepository modelRelationRepository) {
        super(repository);
        this.modelRepository = modelRepository;
        this.modelMetadataRepository = modelMetadataRepository;
        this.columnMetadataRepository = columnMetadataRepository;
        this.tagRepository = tagRepository;
        this.modelRelationRepository = modelRelationRepository;
    }

    public List<Model> findByName(String name, Pageable pageable) {
        return modelRepository.findByName(name, pageable);
    }

    public List<Model> findByFormatType(FormatType formatType, Pageable pageable) {
        return modelRepository.findByFormatType(formatType, pageable);
    }

    public List<Model> findByStorageId(UUID storageId, Pageable pageable) {
        return modelRepository.findByStorageId(storageId, pageable);
    }

    public List<Model> findByStatus(StatusType statusType, Pageable pageable) {
        return modelRepository.findByStatus(statusType, pageable);
    }

    public List<Model> findByCreatedBy(UUID createdBy, Pageable pageable) {
        return modelRepository.findByCreatedBy(createdBy, pageable);
    }

    public List<Model> findByModifiedBy(UUID modifiedBy, Pageable pageable) {
        return modelRepository.findByModifiedBy(modifiedBy, pageable);
    }

    public List<Model> findBySyncEnable(boolean syncEnable, Pageable pageable) {
        return modelRepository.findBySyncEnable(syncEnable, pageable);
    }

    public List<Model> findByModelMetadataValue(String metadataValue, Pageable pageable) {
        var modelMetadataList = modelMetadataRepository.findByMetadataValue(metadataValue);
        // todo pageable 적용하기

        var modelList = new ArrayList<Model>();
        for (var modelMetadata : modelMetadataList) {
            var model = modelRepository.findById(modelMetadata.getModelId());
            model.ifPresent(modelList::add);
        }

        return modelList;
    }

    public List<Model> findByColumnMetadataName(String name, Pageable pageable) {
        var columnMetadataList = columnMetadataRepository.findByName(name);
        // todo pageable 적용하기

        var modelList = new ArrayList<Model>();
        for (var columnMetadata: columnMetadataList) {
            var model = modelRepository.findById(columnMetadata.getModelId());
            model.ifPresent(modelList::add);
        }

        return modelList;
    }

    public List<Model> findByTagValue(String tagValue, Pageable pageable) {
        var tagList = tagRepository.findByTagValue(tagValue);
        // todo pageable 적용

        var modelList = new ArrayList<Model>();
        for (var tag: tagList) {
            var modelTags = tag.getModelTags();
            for (var modelTag: modelTags) {
                var model = modelRepository.findById(modelTag.getModelId());
                model.ifPresent(modelList::add);
            }
        }

        return modelList;
    }

    public List<Model> findByRelationParentId(UUID parentId, Pageable pageable) {
        var targetModel = modelRepository.findById(parentId);
        // todo pageable 적용

        var modelList = new ArrayList<Model>();
        if (targetModel.isPresent()) {
            var relations = targetModel.get().getModelRelations();
            for (var relation: relations) {
                var model = modelRepository.findById(relation.getChildModelId());
                model.ifPresent(modelList::add);
            }
        }

        return modelList;
    }

    public List<Model> findByRelationChildId(UUID childId, Pageable pageable) {
        var relationList = modelRelationRepository.findByChildModelId(childId);
        // todo pageable

        var modelList = new ArrayList<Model>();
        for (var relation: relationList) {
            var relationParentId = relation.getModelId();
            var model = modelRepository.findById(relationParentId);
            model.ifPresent(modelList::add);
        }
        return modelList;
    }

    public List<Model> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return modelRepository.findByCreatedAtBetween(from, to, pageable);
    }

    public List<Model> findByModifiedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return modelRepository.findByModifiedAtBetween(from, to, pageable);
    }

    public List<Model> findByCreatedAtBefore(LocalDateTime endTime, Pageable pageable) {
        return modelRepository.findByCreatedAtBefore(endTime, pageable);
    }

    public List<Model> findByModifiedAtBefore(LocalDateTime endTime, Pageable pageable) {
        return modelRepository.findByModifiedAtBefore(endTime, pageable);
    }

    public List<Model> findByCreatedAtAfter(LocalDateTime startTime, Pageable pageable) {
        return modelRepository.findByCreatedAtAfter(startTime, pageable);
    }

    public List<Model> findByModifiedAtAfter(LocalDateTime startTime, Pageable pageable) {
        return modelRepository.findByModifiedAtAfter(startTime, pageable);
    }
}
