package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.entity.EntityRelationshipEntity;
import com.mobigen.vdap.server.entity.TagEntity;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.models.EntityVersionPair;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.repositories.EntityExtensionRepository;
import com.mobigen.vdap.server.repositories.EntityRelationshipRepository;
import com.mobigen.vdap.server.util.EntityUtil;
import com.mobigen.vdap.server.util.Fields;
import com.mobigen.vdap.server.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
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

@Slf4j
@Service
public class TagService {
    private static final String TAG_API_PATH = "/v1/tags";
    private final TagRepository tagRepository;
    private final Set<String> allowedFields;

    private final TagLabelUtil tagLabelUtil;
    private final EntityRelationshipRepository entityRelationshipRepository;
    private final EntityExtensionRepository entityExtensionRepository;

    public TagService(TagRepository tagRepository,
                      TagLabelUtil tagLabelUtil,
                      EntityRelationshipRepository entityRelationshipRepository,
                      EntityExtensionRepository entityExtensionRepository) {
        this.tagRepository = tagRepository;
        this.tagLabelUtil = tagLabelUtil;
        this.entityRelationshipRepository = entityRelationshipRepository;
        this.entityExtensionRepository = entityExtensionRepository;
        this.allowedFields = Entity.getEntityFields(Tag.class);
    }

    public Fields getFields(String fields) {
        if ("*".equals(fields)) {
            return new Fields(allowedFields, String.join(",", allowedFields));
        }
        return new Fields(allowedFields, fields);
    }


    // List
    public PageModel<Tag> list(URI baseUri, String parent, String fieldsParam, Integer page, Integer size) {
        Fields fields = getFields(fieldsParam);
//        Pageable<> pageable = new PageRequest(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<TagEntity> entities;
        if (!CommonUtil.nullOrEmpty(parent)) {
            entities = tagRepository.findAllByClassificationId(parent, pageable);
        } else {
            entities = tagRepository.findAll(pageable);
        }

        PageModel<Tag> res = new PageModel<>();
        res.setPage(page);
        res.setSize(size);
        if (entities.getTotalElements() > 0) {
            log.info("[Tag] List Result : Page[{}/{}] TotalElements[{}]",
                    pageable.getPageNumber(), pageable.getPageSize(),
                    entities.getTotalElements());
        } else {
            log.info("[Tag] List Result : Not Have Tag");
            res.setTotalElements(0);
            res.setTotalPages(0);
            res.setContents(Collections.emptyList());
            return res;
        }
        // Convert
        List<Tag> tags = entities.getContent().stream().map(this::convertToDto).toList();
        // SetFields
        tags.forEach(tag -> {
            setFields(tag, fields);
            addHref(tag, baseUri);
        });
        res.setTotalElements((int) entities.getTotalElements());
        res.setTotalPages(entities.getTotalPages());
        res.setContents(tags);
        return res;
    }

    // Get Tag By ID
    public Tag getById(URI baseUri, String id, String fieldsParam) {
        Fields fields = getFields(fieldsParam);
        Optional<TagEntity> entity = tagRepository.findById(id);
        if (entity.isEmpty()) {
            throw new CustomException("[Tag] Not Found By Id", id);
        }
        Tag tag = setFields(convertToDto(entity.get()), fields);
        addHref(tag, baseUri);
        return tag;
    }

    public Tag setFields(Tag tag, Fields fields) {
        tag.withClassification(getClassification(tag));
        if (fields.contains(Entity.FIELD_USAGE_COUNT)) {
            tag.withUsageCount(getUsageCount(tag));
        } else {
            tag.withUsageCount(null);
        }
        return tag;
    }

    private EntityReference getClassification(Tag tag) {
        List<EntityRelationshipEntity> relations = entityRelationshipRepository.findByToIdAndToEntityAndRelationAndFromEntity(
                tag.getId().toString(), Entity.TAG, Relationship.CONTAINS.ordinal(), Entity.CLASSIFICATION);
        if (relations.size() != 1) {
            log.warn("[TAG] Possible database issues - multiple relations Classification Num[{}] for Tag[{}/{}]",
                    relations.size(), tag.getId(), tag.getName());
        }
        return tagLabelUtil.getReference(UUID.fromString(relations.getFirst().getFromId()), Entity.CLASSIFICATION);
    }

    private Integer getUsageCount(Tag tag) {
        // TODO : tag usage count
        return 0;
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
        EntityReference classification = tagLabelUtil.getReference(tag.getClassification().getId(), Entity.CLASSIFICATION);
        tag.setClassification(classification);
    }

    private void createNewEntity(Tag tag) {
        storeEntity(tag, false);
        storeRelationshipsInternal(tag);
        // TODO : 검색되도록 설정해야 함.
        // postCreate(tag);
    }

    @Transactional
    public Tag update(URI baseUri, Tag tag) {
        // Find Original
        Optional<TagEntity> originalEntity = tagRepository.findById(tag.getId().toString());
        if (originalEntity.isPresent()) {
            Tag updated = updateInternal(baseUri, convertToDto(originalEntity.get()), tag);
            // TODO : 검색 업데이트
            // postUpdate(updated)
            return updated;
        }
        log.error("[Tag] Update Failed. No data found for the given ID[{}}", tag.getId().toString());
        throw new CustomException("[Tag] Update Failed. No data found for the given ID", tag);
    }

    private Tag updateInternal(URI baseUri, Tag original, Tag updated) {
        // 시간 값 비교 X
        LocalDateTime updatedAt = updated.getUpdatedAt();
        updated.setUpdatedAt(null);
        LocalDateTime originalUpdatedAt = original.getUpdatedAt();
        original.setUpdatedAt(null);
        // Classification 정보의 경우 저장 시 저장하지 않음.
        EntityReference classification = updated.getClassification();
        updated.setClassification(null);
        // 변경정보 비교 X
        String changeDescription = original.getChangeDescription();
        original.setChangeDescription(null);
        updated.setChangeDescription(null);
        // 사용상태 정보 동기화
        updated.setUsageCount(original.getUsageCount());
        // 버전 정보 동기화
        updated.setVersion(original.getVersion());
        // MutuallyExclusive 는 업데이트 되지 않음.
        updated.setMutuallyExclusive(original.getMutuallyExclusive());
        // JSON 을 이용한 데이터 비교
        String patch = JsonUtils.diff(JsonUtils.pojoToJson(original), JsonUtils.pojoToJson(updated));
        log.debug("[Tag] Json Diff - \n{}", patch);
        // 데이터 원복
        updated.setUpdatedAt(updatedAt);
        updated.setClassification(classification);
        // 버전 업
        updated.setVersion(EntityUtil.nextVersion(original.getVersion()));
        // 저장
        storeEntity(updated, true);
        // 히스토리 저장
        original.setUpdatedAt(originalUpdatedAt);
        original.setChangeDescription(changeDescription);
        storeEntityHistory(original);
        addHref(updated, baseUri);
        return updated;
    }


    private void storeEntity(Tag tag, boolean update) {
        // Classification, parent, child, href set null.
        EntityReference classification = tag.getClassification();
        tag.withClassification(null).withHref(null);

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

    private void storeEntityHistory(Tag tag) {
        String extensionName = EntityUtil.getVersionExtension(Entity.TAG, tag.getVersion());
        EntityExtension extension = EntityExtension.builder()
                .id(tag.getId().toString())
                .extension(extensionName)
                .entityType(Entity.TAG)
                .json(JsonUtils.pojoToJson(tag)).build();
        entityExtensionRepository.save(extension);
    }

    @Transactional
    public void deleteById(String id, String deletedBy) {
        log.info("[Tag] Delete By Id[{}] By[{}]", id, deletedBy);
        Optional<TagEntity> tag = tagRepository.findById(id);
        tag.ifPresent(tagEntity -> delete(convertToDto(tagEntity)));
    }

    private void delete(Tag tag) {
        entityRelationshipRepository.deleteByToEntityAndToId(Entity.TAG, tag.getId().toString());
        tagRepository.deleteById(tag.getId().toString());
        // TODO : delete tag usage
        // 1. Delete From Tag Usage
        // 2. Delete From Entity Usage -> may be no need
        // 3. Delete from Search Engine
    }

    // Version

    public EntityHistory listVersions(UUID id) {
        // find
        Optional<TagEntity> tagEntity = tagRepository.findById(id.toString());
        if (tagEntity.isEmpty()) {
            throw new CustomException("[Tag] Get Version History Failed. Not Found By Id", id);
        }
        // id 와 extension(tag.versions.%) 을 이용해 검색
        String extensionPrefix = EntityUtil.getVersionExtensionPrefix(Entity.TAG);
        List<EntityExtension> histories =
                entityExtensionRepository.findByIdAndExtensionStartingWith(id.toString(),
                        extensionPrefix + ".", Sort.by(Sort.Order.desc(Entity.FIELD_EXTENSION)));
        final List<Object> allVersions = new ArrayList<>();
        // Add Latest(Current)
        allVersions.add(JsonUtils.pojoToJson(tagEntity.get().getJson()));
        // And Add Old Version
        histories.forEach(old -> allVersions.add(old.getJson()));
        return new EntityHistory().withEntityType(Entity.TAG).withVersions(allVersions);
    }

    public Tag getVersion(UUID id, String version) {
        Double requestedVersion = Double.parseDouble(version);
        String extension = EntityUtil.getVersionExtension(Entity.TAG, requestedVersion);
        // 버전 히스토리에서 요청한 버전을 검색
        Optional<EntityExtension> entity = entityExtensionRepository.findByIdAndExtension(id.toString(), extension);
        if (entity.isPresent()){
            return JsonUtils.readValue(entity.get().getJson(), Tag.class);
        }
        // 히스토리에서 찾을 수 없는 경우 최신 버전을 확인
        Optional<TagEntity> tagEntity = tagRepository.findById(id.toString());
        if( tagEntity.isPresent() ) {
            Tag tag = convertToDto(tagEntity.get());
            if (tag.getVersion().equals(requestedVersion)) {
                return tag;
            }
        }
        throw new CustomException(String.format("[Tag] ID[%s] Version[%s] Not Found", id, version), null);
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
