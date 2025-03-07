package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.ClassificationEntity;
import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.repositories.EntityExtensionRepository;
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
    private final EntityExtensionRepository entityExtensionRepository;
    private final TagService tagService;

    private static final String CLASSIFICATION_API_PATH = "/v1/classifications";

    public ClassificationService(ClassificationRepository classificationRepository, TagService tagService,
                                 EntityRelationshipRepository entityRelationshipRepository,
                                 EntityExtensionRepository entityExtensionRepository) {
        this.classificationRepository = classificationRepository;
        this.entityRelationshipRepository = entityRelationshipRepository;
        this.tagService = tagService;
        this.entityExtensionRepository = entityExtensionRepository;
        allowFields = Entity.getEntityFields(Classification.class);
    }

    private Fields getFields(String fields) {
        if ("*".equals(fields)) {
            return new Fields(allowFields, String.join(",", allowFields));
        }
        return new Fields(allowFields, fields);
    }

    // List

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

    // Get by Id, Name

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

    private void setFields(Classification classification, Fields fields) {
        classification.withTermCount(
                fields.contains(Entity.FIELD_TERM_COUNT) ? getTermCount(classification) : null);
        classification.withUsageCount(
                fields.contains(Entity.FIELD_USAGE_COUNT) ? getUsageCount(classification) : null);
    }

    @Transactional
    public Classification create(URI baseUri, Classification classification) {
        storeEntity(classification, false);
        addHref(classification, baseUri);
        return classification;
    }

    @Transactional
    public Classification update(URI baseUri, Classification classification) {
        Optional<ClassificationEntity> original = classificationRepository.findById(classification.getId().toString());
        if (original.isPresent()) {
            Classification updated = updateInternal(baseUri, convertToDto(original.get()), classification);
            // TODO : 검색 업데이트
            // postUpdate(updated)
            return updated;
        }
        log.error("[Classification] Update Failed. No data found for the given ID[{}}", classification.getId().toString());
        throw new CustomException("[Classification] Update Failed. No data found for the given ID", classification);
    }

    private Classification updateInternal(URI baseUri, Classification original, Classification updated) {
        // MutuallyExclusive 는 업데이트 되지 않음.
        updated.setMutuallyExclusive(original.getMutuallyExclusive());
        // 업데이트 시간은 비교하지 않음.
        LocalDateTime updatedAt = updated.getUpdatedAt();
        updated.setUpdatedAt(null);
        LocalDateTime originalUpdatedAt = original.getUpdatedAt();
        original.setUpdatedAt(null);
        // 변경정보 비교 X
        String originalChangeDescription = original.getChangeDescription();
        original.setChangeDescription(null);
        updated.setChangeDescription(null);
        // 사용상태 정보 동기화
        updated.setUsageCount(original.getUsageCount());
        updated.setTermCount(original.getTermCount());
        updated.setProvider(original.getProvider());
        // 링크정보
        original.setHref(null);
        // 비교 대상만으로 데이터를 생성하고 비교
        String patch = JsonUtils.diff(JsonUtils.pojoToJson(original), JsonUtils.pojoToJson(updated));
        log.debug("[Classification] Json Diff - \n{}", patch);

        updated.setUpdatedAt(updatedAt);
        updated.setVersion(EntityUtil.nextVersion(original.getVersion()));
        updated.setChangeDescription(patch);

        storeEntity(updated, true);
        original.setUpdatedAt(originalUpdatedAt);
        original.setChangeDescription(originalChangeDescription);
        storeEntityHistory(original);
        addHref(updated, baseUri);
        return updated;
    }

    private void storeEntity(Classification classification, boolean update) {
        // Not save link
        classification.withHref(null);
        // Convert To Entity
        ClassificationEntity entity = new ClassificationEntity();
        entity.setId(classification.getId().toString());
        entity.setName(classification.getName());
        entity.setJson(JsonUtils.pojoToJson(classification));
        entity.setUpdatedAt(classification.getUpdatedAt());
        entity.setUpdatedBy(classification.getUpdatedBy());
        if (update) {
            log.info("[Classification] Updated ID[{}] Name[{}]", entity.getId(), entity.getName());
        } else {
            log.info("[Classification] Created ID[{}] Name[{}]", entity.getId(), entity.getName());
        }
        classificationRepository.save(entity);
    }

    private void storeEntityHistory(Classification classification) {
        String extensionName = EntityUtil.getVersionExtension(Entity.CLASSIFICATION, classification.getVersion());
        EntityExtension extension = EntityExtension.builder()
                .id(classification.getId().toString())
                .extension(extensionName)
                .entityType(Entity.CLASSIFICATION)
                .json(JsonUtils.pojoToJson(classification)).build();
        entityExtensionRepository.save(extension);
    }

    // Delete

    @Transactional
    public void deleteById(UUID id, String userName) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        if (entity.isPresent()) {
            delete(convertToDto(entity.get()), userName);
        } else {
            log.error("[Classification] Deleted Failed. No Data Found For The Given ID[{}]", id);
            throw new CustomException("[Classification] Delete Failed. No data found for the given ID", id);
        }
    }

    @Transactional
    public void deleteByName(String name, String userName) {
        Optional<ClassificationEntity> entity = classificationRepository.findByName(name);
        if (entity.isPresent()) {
            delete(convertToDto(entity.get()), userName);
        } else {
            log.error("[Classification] Deleted Failed. No Data Found For The Given Name[{}]", name);
            throw new CustomException("[Classification] Delete Failed. No data found for the given Name", name);
        }
    }

    private void delete(Classification classification, String userName) {
        checkSystemEntityDeletion(classification);
        deleteChildren(classification.getId(), userName);
//        EventType changeType = EventType.ENTITY_DELETED;
        cleanup(classification);
    }

    private void deleteChildren(UUID id, String deletedBy) {
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
        for (EntityRelationshipEntity c : childrenRecords) {
            log.info("[Classification] Children Recursively deleting Type[{}] Id[{}]", c.getToEntity(), c.getToId());
            if (c.getToEntity().equals(Entity.TAG)) {
                tagService.deleteById(c.getToId(), deletedBy);
            } else {
                throw new CustomException("[Classification] Unsupported child entity", c.getToEntity());
            }
        }
    }

    private void cleanup(Classification classification) {

        // Delete all the relationships to other entities
        log.info("[Classification] Delete Relationship Data ToEntity[{}] ToId[{}]", Entity.CLASSIFICATION, classification.getId().toString());
        entityRelationshipRepository.deleteByToEntityAndToId(Entity.CLASSIFICATION, classification.getId().toString());
        log.info("[Classification] Delete Relationship Data FromEntity[{}] FromId[{}]", Entity.CLASSIFICATION, classification.getId().toString());
        entityRelationshipRepository.deleteByFromEntityAndFromId(Entity.CLASSIFICATION, classification.getId().toString());

        // Delete all the tag labels
        //     tagUsage()
        //     .deleteTagLabelsByTargetPrefix(entityInterface.getFullyQualifiedName());
        log.info("[Classification] Delete Tag Usage Data By Target[{}]", classification.getId().toString());

        // when the glossary and tag is deleted, delete its usage
        // daoCollection.tagUsageDAO().deleteTagLabelsByFqn(entityInterface.getFullyQualifiedName());
        // // Delete all the usage data
        // daoCollection.usageDAO().delete(id);

        // Delete all the threads that are about this entity
        // Entity.getFeedRepository().deleteByAbout(entityInterface.getId());

        // Finally, delete the entity
        log.info("[Classification] Delete Finally By Id[{}] Name[{}]", classification.getId().toString(), classification.getName());
        classificationRepository.deleteById(classification.getId().toString());

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