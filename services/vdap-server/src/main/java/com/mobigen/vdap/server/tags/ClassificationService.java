package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.ClassificationEntity;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.repositories.EntityRelationshipRepository;
import com.mobigen.vdap.server.util.EntityUtil;
import com.mobigen.vdap.server.util.Fields;
import com.mobigen.vdap.server.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import com.mobigen.vdap.server.entity.EntityRelationshipEntity;

@Slf4j
@Service
public class ClassificationService {
    private final Set<String> allowFields;
    private final ClassificationRepository classificationRepository;
    private final EntityRelationshipRepository entityRelationshipRepository;
    private final TagService tagService;

    private static final String CLASSIFICATION_API_PATH = "/v1/classification";

    public ClassificationService(ClassificationRepository classificationRepository, TagService tagService,
            EntityRelationshipRepository entityRelationshipRepository) {
        this.classificationRepository = classificationRepository;
        this.entityRelationshipRepository = entityRelationshipRepository;
        this.tagService = tagService;
        allowFields = Entity.getEntityFields(Classification.class);
    }

    public PageModel<Classification> list(URI baseUri, String fieldsParam, Integer page, Integer size) {
        Fields fields = getFields(fieldsParam);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<ClassificationEntity> entities = classificationRepository.findAll(pageable);
        PageModel<Classification> res = new PageModel<>();
        res.setPage(page);
        res.setSize(size);
        if (entities.getTotalElements() > 0) {
            log.info("[Classification] List Result : Page[{}/{}] TotalElements[{}]",
                    pageable.getPageNumber(), pageable.getPageSize(),
                    entities.getTotalElements());
        } else {
            log.info("[Classification] List Result : Not Have Classification");
            res.setTotalElements(0);
            res.setTotalPages(0);
            res.setContents(Collections.emptyList());
            return res;
        }
        // Convert
        List<Classification> classifications = entities.getContent().stream().map(this::convertToDto).toList();
        // SetFields
        classifications.forEach(classification -> {
            setFields(classification, fields);
            addHref(classification, baseUri);
        });
        res.setTotalElements((int) entities.getTotalElements());
        res.setTotalPages(entities.getTotalPages());
        res.setContents(classifications);
        return res;
    }

    public EntityReference getByRef(UUID id) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        if (entity.isPresent()) {
            Classification classification = convertToDto(entity.get());
            return new EntityReference()
                    .withId(classification.getId())
                    .withType(Entity.CLASSIFICATION)
                    .withName(classification.getName())
                    .withDisplayName(classification.getDisplayName())
                    .withDescription(classification.getDescription());
//                    ;kl
//                    .withInherited(false)
//                    .withHref(URI.create(String.format("%s%s/%s", baseUri.toString(),
//                                    CLASSIFICATION_API_PATH,
//                                    classification.getId().toString())));
        }
        throw new CustomException("[Classification] Not Found By Id For Get Reference", id.toString());
    }

    public Classification getById(URI baseUri, String fieldsParam, UUID id) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        if (entity.isPresent()) {
            return getInternal(entity.get(), baseUri, fieldsParam);
        }
        throw new CustomException("[Classification] Not Found By Id", id.toString());
    }

    public Classification getByName(URI baseUri, String fieldsParam, String name) {
        Optional<ClassificationEntity> entity = classificationRepository.findByName(name);
        if (entity.isPresent()) {
            return getInternal(entity.get(), baseUri, fieldsParam);
        }
        throw new CustomException("[Classification] Not Found By Name", name);
    }

    private Classification getInternal(ClassificationEntity entity, URI baseUri, String fieldsParam) {
        Classification classification = convertToDto(entity);
        Fields fields = getFields(fieldsParam);
        setFields(classification, fields);
        addHref(classification, baseUri);
        return classification;
    }

    @Transactional
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

    @Transactional
    public Classification update(URI baseUri, Classification classification) {
        Optional<ClassificationEntity> original = classificationRepository.findById(classification.getId().toString());
        if (original.isPresent()) {
            return updateInternal(baseUri, convertToDto(original.get()), classification);
        }
        log.error("[Classification] Update Failed. No data found for the given ID[{}}", classification.getId().toString());
        throw new CustomException("[Classification] Update Failed. No data found for the given ID", classification);
    }

    public Classification updateInternal(URI baseUri, Classification original, Classification updated) {
        // MutuallyExclusive 는 업데이트 되지 않음.
        updated.setMutuallyExclusive(original.getMutuallyExclusive());
        // 업데이트 시간은 비교하지 않음.
        LocalDateTime updatedAt = updated.getUpdatedAt();
        updated.setUpdatedAt(null);
        original.setUpdatedAt(null);

        // 비교 대상만으로 데이터를 생성하고 비교
        String patch = JsonUtils.diff(JsonUtils.pojoToJson(original), JsonUtils.pojoToJson(updated));
        log.debug("[Classification] Json Diff - \n{}", patch);

        updated.setUpdatedAt(updatedAt);
        updated.setVersion(EntityUtil.nextVersion(original.getVersion()));

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
            log.info("[Classification] Updated ID[{}] Name[{}]", entity.getId(), entity.getName());
        } else {
            log.info("[Classification] Created ID[{}] Name[{}]", entity.getId(), entity.getName());
        }
    }

    public void deleteById(UUID id, String userName) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        if (entity.isPresent()) {
            delete(convertToDto(entity.get()), userName);
        } else {
            throw new CustomException("[Classification] Delete Failed. No data found for the given ID", id);
        }
    }

    public void deleteByName(String name, String userName) {
        Optional<ClassificationEntity> entity = classificationRepository.findByName(name);
        if (entity.isPresent()) {
            delete(convertToDto(entity.get()), userName);
        } else {
            throw new CustomException("[Classification] Delete Failed. No data found for the given Name", name);
        }
    }

    @Transactional
    protected void delete(Classification classification, String userName) {
        checkSystemEntityDeletion(classification);
        deleteChildren(classification.getId(), userName);
//
//        EventType changeType = EventType.ENTITY_DELETED;
//        log.info("{} deleted {}", "Hard", classification.getName());
//        cleanup(classification);
//        return classification;
    }

    protected void deleteChildren(UUID id, String deletedBy) {
        // If an entity being deleted contains other **non-deleted** children entities, it can't be deleted
        List<EntityRelationshipEntity> childrenRecords =
                        entityRelationshipRepository.findByFromIdAndFromEntityAndRelationIn(
                                id.toString(), Entity.CLASSIFICATION,
                                List.of(Relationship.CONTAINS.ordinal(), Relationship.PARENT_OF.ordinal()));

        if (childrenRecords.isEmpty()) {
            log.info("[Classification] No children to delete. ID[{}]", id);
            return;
        }
        // Delete all the contained entities
        deleteChildren(childrenRecords, deletedBy);
    }

    protected void deleteChildren(List<EntityRelationshipEntity> children, String deletedBy) {
        for (EntityRelationshipEntity c : children) {
            log.info("[Classification] Children Recursively deleting Type[{}] Id[{}]", c.getToEntity(), c.getToId());
            if (c.getToEntity().equals(Entity.TAG)) {
                tagService.deleteById(c.getToId(), deletedBy);
            } else {
                throw new CustomException("[Classification] Unsupported child entity", c.getToEntity());
            }
        }
    }
    protected void cleanup(Classification classification) {

        // // Delete all the relationships to other entities
        // entityRelationshipRepository.deleteAll(ids);(id, entityType);

        // // Delete all the field relationships to other entities
        // daoCollection.fieldRelationshipDAO().deleteAllByPrefix(entityInterface.getFullyQualifiedName());

        //     // Delete all the tag labels
        //     tagUsage()
        //     .deleteTagLabelsByTargetPrefix(entityInterface.getFullyQualifiedName());

        // // when the glossary and tag is deleted, delete its usage
        // daoCollection.tagUsageDAO().deleteTagLabelsByFqn(entityInterface.getFullyQualifiedName());
        // // Delete all the usage data
        // daoCollection.usageDAO().delete(id);

        // // Delete the extension data storing custom properties
        // removeExtension(entityInterface);

        // // Delete all the threads that are about this entity
        // Entity.getFeedRepository().deleteByAbout(entityInterface.getId());

        // // Remove entity from the cache
        // invalidate(entityInterface);

        // // Finally, delete the entity
        // dao.delete(id);
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
        // return tagService.getCountByClassificationId(classification.getId().toString());
        return 0;
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
            throw new CustomException(
                    String.format("[Classification] System entity [%s] can not be deleted.", entity.getName()), null);
        }
    }

    private void addHref(Classification classification, URI baseUri) {
        classification.setHref(URI.create(
                String.format("%s%s/%s", baseUri.toString(),
                        CLASSIFICATION_API_PATH,
                        classification.getId().toString())));
    }
}