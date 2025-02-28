package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.EntityRelationshipEntity;
import com.mobigen.vdap.server.entity.TagEntity;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.repositories.EntityRelationshipRepository;
import com.mobigen.vdap.server.util.Fields;
import com.mobigen.vdap.server.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Slf4j
@Service
public class TagService {
    private static final String TAG_API_PATH = "/v1/tags";
    private final TagRepository tagRepository;
    private final Set<String> allowedFields;

    private final ClassificationService classificationService;
    private final EntityRelationshipRepository entityRelationshipRepository;

    public TagService(TagRepository tagRepository,
                      ClassificationService classificationService,
                      EntityRelationshipRepository entityRelationshipRepository) {
        this.tagRepository = tagRepository;
        this.classificationService = classificationService;
        this.entityRelationshipRepository = entityRelationshipRepository;
        this.allowedFields = Entity.getEntityFields(Tag.class);
    }

    public Fields getFields(String fields) {
        if ("*".equals(fields)) {
            return new Fields(allowedFields, String.join(",", allowedFields));
        }
        return new Fields(allowedFields, fields);
    }


    // List

    // Get Tag By ID
    public Tag getInternalById(URI baseUri, String id, String fieldsParam) {
        Fields fields = getFields(fieldsParam);
        Optional<TagEntity> entity = tagRepository.findById(id);
        if(entity.isEmpty()) {
            throw new CustomException("[Tag] Not Found By Id", id);
        }
        Tag tag = setFieldsInternal(convertToDto(entity.get()), fields);
        clearFieldsInternal(tag, fields);
        addHref(tag, baseUri);
        return tag;
    }

    public Tag setFieldsInternal(Tag tag, Fields fields) {
        tag.setChildren(fields.contains(Entity.FIELD_CHILDREN) ? getChildren(tag) : tag.getChildren());
        tag.withClassification(getClassification(tag));
        if (fields.contains("usageCount")) {
            tag.withUsageCount(getUsageCount(tag));
        }
        return tag;
    }

    private List<EntityReference> getChildren(Tag tag) {
        List<EntityRelationshipEntity> list = entityRelationshipRepository.findByFromIdAndFromEntityAndToEntityAndRelation(
                tag.getId().toString(), Entity.TAG, Entity.TAG, Relationship.CONTAINS.ordinal());
        List<EntityReference> refList = new ArrayList<>();
        list.forEach(entity -> {
            refList.add(this.getRefById(entity.getToId()));
        });
        return refList;
    }

    private Integer getUsageCount(Tag tag) {
        // TODO : tag usage count
        return 0;
    }

    private void clearFieldsInternal(Tag tag, Fields fields) {
        tag.setChildren(fields.contains(Entity.FIELD_CHILDREN) ? tag.getChildren() : null);
        tag.withUsageCount(fields.contains("usageCount") ? tag.getUsageCount() : null);
    }

    // Get Reference(Tag)
    public EntityReference getRefById(String id) {
        Optional<TagEntity> entity = tagRepository.findById(id);
        if(entity.isEmpty()) {
            throw new CustomException("[Tag] Not Found By ID", id);
        }
        Tag tag = convertToDto(entity.get());

        return new EntityReference()
                .withId(tag.getId())
                .withType(Entity.TAG)
                .withName(tag.getName())
                .withDisplayName(tag.getDisplayName())
                .withDescription(tag.getDescription());
    }

    @Transactional
    public Tag create(URI baseUri, Tag tag) {
        prepareInternal(tag);
        createNewEntity(tag);
        addHref(tag, baseUri);
        return tag;
    }

    private void prepareInternal(Tag tag) {
        // Validate Classification
        EntityReference classification = classificationService.getByRef(tag.getClassification().getId());
        tag.setClassification(classification);
    }

    private Tag createNewEntity(Tag entity) {
        storeEntity(entity, false);
        storeRelationshipsInternal(entity);
        // TODO : 검색되도록 설정해야 함.
//        postCreate(entity);
        return entity;
    }

    private void storeEntity(Tag tag, boolean update) {
        // Classification, parent, child, href set null.
        EntityReference classification = tag.getClassification();
        tag.withClassification(null).withParent(null).withHref(null);
        List<EntityReference> children = tag.getChildren();
        tag.setChildren(null);

        TagEntity entity = TagEntity.builder()
                .id(tag.getId().toString())
                .name(tag.getName())
                .classificationId(classification.getId().toString())
                .json(JsonUtils.pojoToJson(tag))
                .updatedAt(tag.getUpdatedAt())
                .updatedBy(tag.getUpdatedBy())
                .build();
        if (update) {
            log.info("[Tag] Updated ID[{}] Name[{}]", entity.getId(), entity.getName());
        } else {
            log.info("[Tag] Created ID[{}] Name[{}]", entity.getId(), entity.getName());
        }
        tagRepository.save(entity);

        // Restore the relationships
        tag.setChildren(children);
        tag.withClassification(classification);
    }

    private void storeRelationshipsInternal(Tag tag) {
        EntityRelationshipEntity entity = new EntityRelationshipEntity();
        // From Classification
        entity.setFromId(tag.getClassification().getId().toString());
        entity.setFromEntity(Entity.CLASSIFICATION);
        // To
        entity.setToId(tag.getId().toString());
        entity.setToEntity(Entity.TAG);
        // RelationType
        entity.setRelation(Relationship.CONTAINS.ordinal());
        // Json
        entity.setJsonSchema(null);
        entity.setJson(null);

        log.info("[Tag] Insert Relationship From[{}/{}] To[{}/{}] RelationType[{}/{}]",
                entity.getFromEntity(), entity.getFromId(), entity.getToEntity(), entity.getToId(),
                Relationship.CONTAINS.value(), entity.getRelation());
        entityRelationshipRepository.save(entity);
    }

    // Delete
    public void deleteInternalById(String id, String deletedBy) {
        log.info("[Tag] Delete By Id[ {} ]", id);
        Optional<TagEntity> tag = tagRepository.findById(id);
        tag.ifPresent(tagEntity -> delete(convertToDto(tagEntity), deletedBy));
    }

    public Tag delete(Tag original, String deletedBy) {
//        checkSystemEntityDeletion(original);
//        preDelete(original, deletedBy);
//        setFieldsInternal(original, putFields);
//        deleteChildren(original.getId(), recursive, hardDelete, deletedBy);
//
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
        return null;
    }

    private Tag convertToDto(TagEntity entity) {
        return JsonUtils.readValue(entity.getJson(), Tag.class);
    }

    private void addHref(Tag tag, URI baseUri) {
        tag.setHref(URI.create(
                String.format("%s%s/%s", baseUri.toString(), TAG_API_PATH, tag.getId().toString())
        ));
    }
}
