package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.ClassificationEntity;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.util.EntityUtil;
import com.mobigen.vdap.server.util.Fields;
import com.mobigen.vdap.server.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class ClassificationService {
    private final ClassificationRepository classificationRepository;
    private final Set<String> allowFields;
    private final TagRepository tagRepository;
    private static final String CLASSIFICATION_API_PATH = "/v1/classification";

    public ClassificationService(ClassificationRepository classificationRepository, TagRepository tagRepository) {
        this.classificationRepository = classificationRepository;
        allowFields = Entity.getEntityFields(Classification.class);
        this.tagRepository = tagRepository;
    }

    public Page<Classification> list(URI baseUri, String fieldsParam, Integer page, Integer size) {
        Fields fields = getFields(fieldsParam);
        Pageable pageable = PageRequest.of(page, size);
        Page<ClassificationEntity> entities = classificationRepository.findAll(pageable);
        // Convert
        Page<Classification> classifications = entities.map(this::convertToDto);
        // For SetFields
        List<Classification> classificationList = classifications.getContent();
        // SetFields
        classificationList.forEach(classification -> {
            setFields(classification, fields);
            addHref(classification, baseUri);
        });
        // 수정된 리스트를 다시 Page로 변환
        return new PageImpl<>(classificationList, pageable, classifications.getTotalElements());
    }

    public Classification getById(URI baseUri, String fieldsParam, UUID id) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        if (entity.isPresent()) {
            return getInternal(entity.get(), baseUri, fieldsParam);
        }
        return null;
    }

    public Classification getByName(URI baseUri, String fieldsParam, String name) {
        Optional<ClassificationEntity> entity = classificationRepository.findByName(name);
        if (entity.isPresent()) {
            return getInternal(entity.get(), baseUri, fieldsParam);
        }
        return null;
    }

    private Classification getInternal(ClassificationEntity entity, URI baseUri, String fieldsParam) {
        Classification classification = convertToDto(entity);
        Fields fields = getFields(fieldsParam);
        setFields(classification, fields);
        addHref(classification, baseUri);
        return classification;
    }

    public Classification create(URI baseUri, Classification classification) {
        ClassificationEntity entity = new ClassificationEntity();
        entity.setId(classification.getId().toString());
        entity.setName(classification.getName());
        entity.setJson(JsonUtils.pojoToJson(classification));
        entity.setUpdatedAt(classification.getUpdatedAt());
        entity.setUpdatedBy(classification.getUpdatedBy());

        storeEntity(entity, false);
        addHref(classification, baseUri);
        // elasticsearch 에 데이터를 저장하는 부분은 classification의 경우 필요하지 않음.
        return classification;
    }

    public Classification createOrUpdate(URI baseUri, Classification classification) {
        // If entity does not exist, this is a create operation, else update operation
        Optional<ClassificationEntity> original = classificationRepository.findByName(classification.getName());
        if (original.isPresent()) {
            // update
            return update(baseUri, convertToDto(original.get()), classification);
        } else {
            // create
            return create(baseUri, classification);
        }
    }

    public Classification update(URI baseUri, Classification original, Classification updated) {
        // Copy Original UUID
        updated.setId(original.getId());
        // MutuallyExclusive 는 업데이트 되지 않음.
        updated.setMutuallyExclusive(original.getMutuallyExclusive());

        // 비교 대상만으로 데이터를 생성하고 비교

        String patch = JsonUtils.diff(JsonUtils.pojoToJson(original), JsonUtils.pojoToJson(updated));
        log.info("[Classification] Update : \n{}", patch);
        // TODO : patch 확인 필요
        updated.setVersion(EntityUtil.nextVersion(original.getVersion()));

//        updateInternal();
        ClassificationEntity updateEntity = new ClassificationEntity();
        updateEntity.setId(updated.getId().toString());
        updateEntity.setName(updated.getName());
        updateEntity.setJson(JsonUtils.pojoToJson(updated));
        updateEntity.setUpdatedAt(updated.getUpdatedAt());
        updateEntity.setUpdatedBy(updated.getUpdatedBy());
        storeEntity(updateEntity, true);

        addHref(updated, baseUri);
        return updated;
    }

    private void storeEntity(ClassificationEntity entity, boolean update) {
        classificationRepository.save(entity);
        if (update) {
            log.info("Updated Classification ID[{}] Name[{}]", entity.getId(), entity.getName());
        } else {
            log.info("Created Classification ID[{}] Name[{}]", entity.getId(), entity.getName());
        }
    }

    public void deleteById(UUID id) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        if (entity.isPresent()) {
            delete(convertToDto(entity.get()));
        } else {
            throw new CustomException(String.format("classification instance for %s not found", id), id);
        }
    }

    public void deleteByName(String name) {
        Optional<ClassificationEntity> entity = classificationRepository.findByName(name);
        if (entity.isPresent()) {
            delete(convertToDto(entity.get()));
        } else {
            throw new CustomException(String.format("classification instance for %s not found", name), name);
        }
    }

    private void delete(Classification classification) {
        checkSystemEntityDeletion(classification);
//        deleteChildren(classification.getId(), true, true, deletedBy);
//        EventType changeType;
//        T updated = get(null, original.getId(), putFields, ALL, false);
//        if (supportsSoftDelete && !hardDelete) {
//            updated.setUpdatedBy(deletedBy);
//            updated.setUpdatedAt(System.currentTimeMillis());
//            updated.setDeleted(true);
//            EntityUpdater updater = getUpdater(original, updated, Operation.SOFT_DELETE);
//            updater.update();
//            changeType = ENTITY_SOFT_DELETED;
//        } else {
//            cleanup(updated);
//            changeType = ENTITY_DELETED;
//        }
//        LOG.info("{} deleted {}", hardDelete ? "Hard" : "Soft", updated.getFullyQualifiedName());
//        return new DeleteResponse<>(updated, changeType);
    }

    @Transactional
    protected void deleteChildren(UUID id, boolean recursive, boolean hardDelete, String updatedBy) {
//        // If an entity being deleted contains other **non-deleted** children entities, it can't be deleted
//        List<EntityRelationshipRecord> childrenRecords =
//                        .relationshipDAO()
//                        .findTo(
//                                id,
//                                entityType,
//                                List.of(Relationship.CONTAINS.ordinal(), Relationship.PARENT_OF.ordinal()));
//
//        if (childrenRecords.isEmpty()) {
//            log.info("No children to delete");
//            return;
//        }
//        // Entity being deleted contains children entities
//        if (!recursive) {
//            throw new IllegalArgumentException(CatalogExceptionMessage.entityIsNotEmpty(entityType));
//        }
//        // Delete all the contained entities
//        deleteChildren(childrenRecords, hardDelete, updatedBy);
    }


    private Fields getFields(String fields) {
        if ("*".equals(fields)) {
            return new Fields(allowFields, String.join(",", allowFields));
        }
        return new Fields(allowFields, fields);
    }

    private void setFields(Classification classification, Fields fields) {
        classification.withTermCount(
                fields.contains("termCount") ? getTermCount(classification) : null);
        classification.withUsageCount(
                fields.contains("usageCount") ? getUsageCount(classification) : null);
    }

    private Integer getTermCount(Classification classification) {
        return tagRepository.getCountByClassificationId(classification.getId().toString());
    }

    private Integer getUsageCount(Classification classification) {
        // TODO : getUsageCount
//        return daoCollection
//                .tagUsageDAO()
//                .getTagCount(TagSource.CLASSIFICATION.ordinal(), classification.getFullyQualifiedName());
        return 0;
    }

    private Classification convertToDto(ClassificationEntity entity) {
        return JsonUtils.readValue(entity.getJson(), Classification.class);
    }

    private void checkSystemEntityDeletion(Classification entity) {
        if (ProviderType.SYSTEM.equals(entity.getProvider())) { // System provided entity can't be deleted
            throw new IllegalArgumentException(
                    String.format("System entity [%s] of classification can not be deleted.", entity.getName()));
        }
    }

    private void addHref(Classification classification, URI baseUri) {
        classification.setHref(URI.create(
                String.format("%s/%s/%s", baseUri.toString(),
                        CLASSIFICATION_API_PATH,
                        classification.getId().toString())));
    }
}
