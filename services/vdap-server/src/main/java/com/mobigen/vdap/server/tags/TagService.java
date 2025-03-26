package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.classification.Tag;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.entity.EntityRelationshipEntity;
import com.mobigen.vdap.server.entity.TagEntity;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.repositories.EntityExtensionRepository;
import com.mobigen.vdap.server.repositories.EntityRelationshipRepository;
import com.mobigen.vdap.server.repositories.TagUsageRepository;
import com.mobigen.vdap.server.util.EntityUtil;
import com.mobigen.vdap.server.util.Fields;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.RestUtil;
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

import static com.mobigen.vdap.schema.type.TagLabel.TagSource.CLASSIFICATION;

@Slf4j
@Service
public class TagService {
    private final TagRepository tagRepository;
    private final Set<String> allowedFields;

    private final TagLabelUtil tagLabelUtil;
    private final EntityRelationshipRepository entityRelationshipRepository;
    private final EntityExtensionRepository entityExtensionRepository;
    private final TagUsageRepository tagUsageRepository;

    public TagService(TagRepository tagRepository,
                      TagLabelUtil tagLabelUtil,
                      EntityRelationshipRepository entityRelationshipRepository,
                      EntityExtensionRepository entityExtensionRepository,
                      TagUsageRepository tagUsageRepository) {
        this.tagRepository = tagRepository;
        this.tagLabelUtil = tagLabelUtil;
        this.entityRelationshipRepository = entityRelationshipRepository;
        this.entityExtensionRepository = entityExtensionRepository;
        this.tagUsageRepository = tagUsageRepository;
        this.allowedFields = Entity.getEntityFields(Tag.class);
    }

    public Fields getFields(String fields) {
        if ("*".equals(fields)) {
            return new Fields(allowedFields, String.join(",", allowedFields));
        }
        return new Fields(allowedFields, fields);
    }

    // list : tag 목록을 반환
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
        log.info("[Tag] Get By Id[{}] Request Fields[{}]", id, fieldsParam);
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
        log.info("[Tag] ID[{}] Get Classification From Relationship By ToEntity[{}]", tag.getId(), Entity.TAG);
        List<EntityRelationshipEntity> relations = entityRelationshipRepository.findByToIdAndToEntityAndRelationAndFromEntity(
                tag.getId().toString(), Entity.TAG, Relationship.CONTAINS.ordinal(), Entity.CLASSIFICATION);
        if (relations.size() != 1) {
            log.warn("[TAG] Possible database issues - multiple relations Classification Num[{}] for Tag[{}/{}]",
                    relations.size(), tag.getId(), tag.getName());
        }
        return tagLabelUtil.getReference(UUID.fromString(relations.getFirst().getFromId()), Entity.CLASSIFICATION);
    }

    private Integer getUsageCount(Tag tag) {
        Integer usageCount = tagUsageRepository.countBySourceAndTagId(CLASSIFICATION.ordinal(), tag.getId().toString());
        log.info("[Tag] Id[{}] Name[{}]. Usage Count[{}]", tag.getId().toString(), tag.getName(), usageCount);
        return usageCount;
    }

    // create : tag 저장 과정이 포함된 메소드
    @Transactional
    public Tag create(URI baseUri, Tag tag) {
        createNewEntity(tag);
        addHref(tag, baseUri);
        return tag;
    }

    // createNewEntity : tag 데이터 저장, 관계 정보 저장, 검색엔진에 저장을 수행
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
        String originalChangeDescription = original.getChangeDescription();
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
        log.debug("[Tag] ID[{}] Name[{}] Json Diff - \n{}", original.getId().toString(), original.getName(), patch);
        // 데이터 원복
        updated.setUpdatedAt(updatedAt);
        updated.setClassification(classification);
        // 버전 정보 와 실제 변경사항 저장
        updated.setChangeDescription(patch);
        updated.setVersion(EntityUtil.nextVersion(original.getVersion()));
        // 저장
        storeEntity(updated, true);
        // 원본 데이터 정보를 복구(원상태)하고 히스토리 저장
        original.setUpdatedAt(originalUpdatedAt);
        original.setChangeDescription(originalChangeDescription);
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
        log.info("[Tag] ID[{}] Name[{}] Store Version History Version[{}]",
                tag.getId().toString(), tag.getName(), extensionName);
        EntityExtension extension = EntityExtension.builder()
                .id(tag.getId().toString())
                .extension(extensionName)
                .entityType(Entity.TAG)
                .json(JsonUtils.pojoToJson(tag)).build();
        entityExtensionRepository.save(extension);
    }

    // listVersions : tag 의 버전 히스토리를 반환
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
        allVersions.add(JsonUtils.pojoToJson(convertToDto(tagEntity.get())));
        // And Add Old Version
        histories.forEach(old -> allVersions.add(old.getJson()));
        return new EntityHistory().withEntityType(Entity.TAG).withVersions(allVersions);
    }

    // getVersion : tag 의 특정 버전을 반환
    public Tag getVersion(UUID id, String version) {
        Double requestedVersion = Double.parseDouble(version);
        String extension = EntityUtil.getVersionExtension(Entity.TAG, requestedVersion);
        // 버전 히스토리에서 요청한 버전을 검색
        Optional<EntityExtension> entity = entityExtensionRepository.findByIdAndExtension(id.toString(), extension);
        if (entity.isPresent()) {
            return JsonUtils.readValue(entity.get().getJson(), Tag.class);
        }
        // 히스토리에서 찾을 수 없는 경우 최신 버전을 확인
        Optional<TagEntity> tagEntity = tagRepository.findById(id.toString());
        if (tagEntity.isPresent()) {
            Tag tag = convertToDto(tagEntity.get());
            if (tag.getVersion().equals(requestedVersion)) {
                return tag;
            }
        }
        throw new CustomException(String.format("[Tag] ID[%s] Version[%s] Not Found", id, version), null);
    }

    // deleteById : tag 를 삭제
    @Transactional
    public void deleteById(String id, String deletedBy) {
        log.info("[Tag] Delete By Id[{}] By[{}]", id, deletedBy);
        Optional<TagEntity> tag = tagRepository.findById(id);
        tag.ifPresent(tagEntity -> delete(convertToDto(tagEntity)));
    }

    // delete : tag 를 삭제
    private void delete(Tag tag) {
        checkSystemEntityDeletion(tag);
        // TODO : Delete Children ( 트리 구조의 Tag 를 추가할 수 있도록 하는 경우 추가 필요 )
        // deleteChildren(....);
        cleanup(tag);
    }

    // cleanup : tag 관련 데이터들을 정리
    private void cleanup(Tag tag) {
        // 연관관계 정보 테이블에서 삭제
        log.info("[Tag] ID[{}] Name[{}] Delete Relationship Data By ToEntity[{}]",
                tag.getId().toString(), tag.getName(), Entity.TAG);
        entityRelationshipRepository.deleteByToEntityAndToId(Entity.TAG, tag.getId().toString());
        log.info("[Tag] ID[{}] Name[{}] Delete Relationship Data By FromEntity[{}]",
                tag.getId().toString(), tag.getName(), Entity.TAG);
        entityRelationshipRepository.deleteByFromEntityAndFromId(Entity.TAG, tag.getId().toString());
        // Delete the extension data
        log.info("[Tag] ID[{}] Name[{}] Delete Extension Data By ID", tag.getId().toString(), tag.getName());
        String versionPrefix = EntityUtil.getVersionExtensionPrefix(Entity.TAG);
        entityExtensionRepository.deleteByIdAndExtensionStartingWith(tag.getId().toString(), versionPrefix + ".");
        // Delete the usage data
        log.info("[Tag] ID[{}] Name[{}] Delete Tag Usage Data By TagId", tag.getId().toString(), tag.getName());
        tagUsageRepository.deleteByTagId(tag.getId().toString());
        // TODO : 사용 히스토리 삭제
        // daoCollection.usageDAO().delete(id);
        // Finally, delete the entity
        log.info("[Tag] ID[{}] Name[{}] Finally Delete", tag.getId().toString(), tag.getName());
        tagRepository.deleteById(tag.getId().toString());
    }

    // getCountByClassificationId : classificationId 에 해당하는 tag 의 개수를 반환
    public Integer getCountByClassificationId(String classificationId) {
        return tagRepository.countByClassificationId(classificationId);
    }

    // convertToDto : TagEntity 를 Tag DTO 로 변환
    private Tag convertToDto(TagEntity entity) {
        return JsonUtils.readValue(entity.getJson(), Tag.class);
    }

    private void checkSystemEntityDeletion(Tag entity) {
        if (ProviderType.SYSTEM.equals(entity.getProvider())) { // System provided entity can't be deleted
            throw new CustomException(
                    String.format("[Tag] System entity [%s] can not be deleted.", entity.getName()), null);
        }
    }

    // addHref : Tag 에 Href 를 추가
    private void addHref(Tag tag, URI baseUri) {
        tag.setHref(RestUtil.getHref(baseUri,
                RestUtil.getControllerBasePath(TagController.class), tag.getId()));
    }
}
