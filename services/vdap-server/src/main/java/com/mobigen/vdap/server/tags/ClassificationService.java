package com.mobigen.vdap.server.tags;

import com.mobigen.vdap.schema.entity.classification.Classification;
import com.mobigen.vdap.schema.type.EntityHistory;
import com.mobigen.vdap.schema.type.ProviderType;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.ClassificationEntity;
import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.entity.EntityRelationshipEntity;
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
public class ClassificationService {
    private final Set<String> allowFields;
    private final ClassificationRepository classificationRepository;
    private final EntityRelationshipRepository entityRelationshipRepository;
    private final EntityExtensionRepository entityExtensionRepository;
    private final TagService tagService;
    private final TagUsageRepository tagUsageRepository;

    public ClassificationService(ClassificationRepository classificationRepository, TagService tagService,
                                 EntityRelationshipRepository entityRelationshipRepository,
                                 EntityExtensionRepository entityExtensionRepository, TagUsageRepository tagUsageRepository) {
        this.classificationRepository = classificationRepository;
        this.entityRelationshipRepository = entityRelationshipRepository;
        this.tagService = tagService;
        this.entityExtensionRepository = entityExtensionRepository;
        this.tagUsageRepository = tagUsageRepository;
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
        // 사용자가 요청한 추가 정보 확인
        Fields fields = getFields(fieldsParam);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<ClassificationEntity> entities = classificationRepository.findAll(pageable);
        PageModel<Classification> res = new PageModel<>();
        res.setPage(page);
        res.setSize(size);
        if (entities.getTotalElements() <= 0) {
            log.info("[Classification] List Result : Not Have Classification");
            res.setTotalElements(0);
            res.setTotalPages(0);
            res.setContents(Collections.emptyList());
            return res;
        }
        log.info("[Classification] List Result : Page[{}/{}] TotalElements[{}]",
                pageable.getPageNumber(), pageable.getPageSize(), entities.getTotalElements());
        // Convert : Entity -> DTO
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

    // Get by ID
    public Classification getById(URI baseUri, String fieldsParam, UUID id) {
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        if (entity.isPresent()) {
            return getInternal(entity.get(), baseUri, fieldsParam);
        }
        throw new CustomException("[Classification] Not Found By Id", id.toString());
    }

    // Get by Name
    public Classification getByName(URI baseUri, String fieldsParam, String name) {
        Optional<ClassificationEntity> entity = classificationRepository.findByName(name);
        if (entity.isPresent()) {
            return getInternal(entity.get(), baseUri, fieldsParam);
        }
        throw new CustomException("[Classification] Not Found By Name", name);
    }

    private Classification getInternal(ClassificationEntity entity, URI baseUri, String fieldsParam) {
        // Entity -> DTO
        Classification classification = convertToDto(entity);
        // 사용자가 요청한 추가 정보 확인
        Fields fields = getFields(fieldsParam);
        // 필드 채우기
        setFields(classification, fields);
        // 링크 추가
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
            return updateInternal(baseUri, convertToDto(original.get()), classification);
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
        // 이전 버전에서 변경 내용 역시 비교하지 않음.
        String originalChangeDescription = original.getChangeDescription();
        original.setChangeDescription(null);
        updated.setChangeDescription(null);
        // 사용상태 정보 동기화하여 비교 대상에서 제외
        updated.setUsageCount(original.getUsageCount());
        updated.setTermCount(original.getTermCount());
        updated.setProvider(original.getProvider());
        // 링크정보 제외
        original.setHref(null);
        // 동기화 및 비교 대상에서 제외된 데이터 외 데이터들을 이용해 JSON Diff 를 수행
        String patch = JsonUtils.diff(JsonUtils.pojoToJson(original), JsonUtils.pojoToJson(updated));
        log.debug("[Classification] ID[{}] Name[{}] Json Diff - \n{}",
                original.getId().toString(), original.getName(), patch);
        // 변경된 데이터 저장을 위해 정보 설정
        updated.setUpdatedAt(updatedAt);
        updated.setVersion(EntityUtil.nextVersion(original.getVersion()));
        updated.setChangeDescription(patch);
        // 저장
        storeEntity(updated, true);
        // 기존 데이터 복구 후 히스토리에 저장
        original.setUpdatedAt(originalUpdatedAt);
        original.setChangeDescription(originalChangeDescription);
        storeEntityHistory(original);
        // 신규 데이터에 링크 정보 추가
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
            log.info("[Classification] ID[{}] Name[{}] Updated", entity.getId(), entity.getName());
        } else {
            log.info("[Classification] ID[{}] Name[{}] Created", entity.getId(), entity.getName());
        }
        classificationRepository.save(entity);
    }

    private void storeEntityHistory(Classification classification) {
        String extensionName = EntityUtil.getVersionExtension(Entity.CLASSIFICATION, classification.getVersion());
        log.info("[Classification] ID[{}] Name[{}] Save Extension Name(Version)[{}]",
                classification.getId().toString(), classification.getName(), extensionName);
        EntityExtension extension = EntityExtension.builder()
                .id(classification.getId().toString())
                .extension(extensionName)
                .entityType(Entity.CLASSIFICATION)
                .json(JsonUtils.pojoToJson(classification)).build();
        entityExtensionRepository.save(extension);
    }

    // listVersions : classification 의 버전 히스토리를 반환
    public EntityHistory listVersions(UUID id) {
        log.info("[Classification] Get Versions By ID[{}]", id);
        Optional<ClassificationEntity> classificationEntity = classificationRepository.findById(id.toString());
        if (classificationEntity.isEmpty()) {
            throw new CustomException("[Classification] Get Version History Failed. Not Found By Id", id);
        }
        // id 와 extension(tag.versions.%) 을 이용해 검색
        String extensionPrefix = EntityUtil.getVersionExtensionPrefix(Entity.CLASSIFICATION);
        List<EntityExtension> histories =
                entityExtensionRepository.findByIdAndExtensionStartingWith(id.toString(),
                        extensionPrefix + ".", Sort.by(Sort.Order.desc(Entity.FIELD_EXTENSION)));
        final List<Object> allVersions = new ArrayList<>();
        // Add Latest(Current)
        allVersions.add(JsonUtils.pojoToJson(convertToDto(classificationEntity.get())));
        // And Add Old Version
        histories.forEach(old -> allVersions.add(old.getJson()));
        return new EntityHistory().withEntityType(Entity.CLASSIFICATION).withVersions(allVersions);
    }

    // getVersion : classification 의 특정 버전을 반환
    public Classification getVersion(UUID id, String version) {
        log.info("[Classification] Get Version By ID[{}] Version[{}]", id, version);
        Double requestedVersion = Double.parseDouble(version);
        String extension = EntityUtil.getVersionExtension(Entity.CLASSIFICATION, requestedVersion);
        // 버전 히스토리에서 요청한 버전을 검색
        Optional<EntityExtension> entity = entityExtensionRepository.findByIdAndExtension(id.toString(), extension);
        if (entity.isPresent()) {
            return JsonUtils.readValue(entity.get().getJson(), Classification.class);
        }
        // 히스토리에서 찾을 수 없는 경우 최신 버전을 확인
        Optional<ClassificationEntity> classificationEntity = classificationRepository.findById(id.toString());
        if (classificationEntity.isPresent()) {
            Classification classification = convertToDto(classificationEntity.get());
            if (classification.getVersion().equals(requestedVersion)) {
                return classification;
            }
        }
        log.error("[Classification] Not Found From Id[{}] And Version[{}]", id, version);
        throw new CustomException(String.format("[Classification] Not Found By Id[%s] And Version[%s]",
                id, version), null);
    }

    // Delete by ID
    @Transactional
    public void deleteById(UUID id, String userName) {
        log.info("[Classification] Delete By ID[{}]", id);
        Optional<ClassificationEntity> entity = classificationRepository.findById(id.toString());
        if (entity.isPresent()) {
            delete(convertToDto(entity.get()), userName);
        } else {
            log.error("[Classification] Deleted Failed. No Data Found For The Given ID[{}]", id);
            throw new CustomException("[Classification] Delete Failed. No data found for the given ID", id);
        }
    }

    // Delete by Name
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
        // 시스템 설정 classification 일 경우 삭제 불가
        checkSystemEntityDeletion(classification);
        // Classification 에 자식(Tag) 삭제
        deleteChildren(classification.getId(), userName);
        // 최종적으로 cleanup 수행
        cleanup(classification);
    }

    private void deleteChildren(UUID id, String deletedBy) {
        // Relationship Repository 에서 삭제하려는 Classification 에 속한 모든 Tag 검색
        List<EntityRelationshipEntity> childrenRecords =
                entityRelationshipRepository.findByFromIdAndFromEntityAndRelationIn(
                        id.toString(), Entity.CLASSIFICATION,
                        List.of(Relationship.CONTAINS.ordinal(), Relationship.PARENT_OF.ordinal()));

        if (childrenRecords.isEmpty()) {
            log.info("[Classification] No Have children. ID[{}]", id);
            return;
        }
        // Delete all the contained entities
        for (EntityRelationshipEntity c : childrenRecords) {
            log.info("[Classification] Children Recursively Delete. Child Type[{}] Id[{}]", c.getToEntity(), c.getToId());
            if (c.getToEntity().equals(Entity.TAG)) {
                tagService.deleteById(c.getToId(), deletedBy);
            } else {
                throw new CustomException("[Classification] Unsupported Child Entity Type", c.getToEntity());
            }
        }
    }

    private void cleanup(Classification classification) {
        // 연관관계 정보 테이블에서 삭제
        log.info("[Classification] ID[{}] Name[{}] Delete Relationship Data By ToEntity[{}]",
                classification.getId().toString(), classification.getName(), Entity.CLASSIFICATION);
        entityRelationshipRepository.deleteByToEntityAndToId(Entity.CLASSIFICATION, classification.getId().toString());
        log.info("[Classification] ID[{}] Name[{}] Delete Relationship Data By FromEntity[{}]",
                classification.getId().toString(), classification.getName(), Entity.CLASSIFICATION);
        entityRelationshipRepository.deleteByFromEntityAndFromId(Entity.CLASSIFICATION, classification.getId().toString());
        // Delete all the extensions of entity
        log.info("[Classification] ID[{}] Name[{}] Delete Extension Data By ID",
                classification.getId().toString(), classification.getName());
        String versionPrefix = EntityUtil.getVersionExtensionPrefix(Entity.CLASSIFICATION);
        entityExtensionRepository.deleteByIdAndExtensionStartingWith(classification.getId().toString(), versionPrefix + ".");
        // Finally, delete the entity
        log.info("[Classification] Id[{}] Name[{}] Finally Delete", classification.getId().toString(), classification.getName());
        classificationRepository.deleteById(classification.getId().toString());
    }

    // getTermCount : Classification 에 속한 Tag 의 개수를 반환
    private Integer getTermCount(Classification classification) {
        Integer term = tagService.getCountByClassificationId(classification.getId().toString());
        log.info("[Classification] ID[{}] Name[{}] Get Term Count[{}]",
                classification.getId().toString(),
                classification.getName(), term);
        return term;
    }

    // getUsageCount : Classification 에 속한 Tag 가 사용된 횟수를 반환
    private Integer getUsageCount(Classification classification) {
        Integer usageCount = tagUsageRepository.countBySourceAndSourceId(
                CLASSIFICATION.ordinal(), classification.getId().toString());
        log.info("[Classification] ID[{}] Name[{}] Get Usage Count[{}]",
                classification.getId().toString(),
                classification.getName(), usageCount);
        return usageCount;
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
        classification.setHref(RestUtil.getHref(baseUri,
                RestUtil.getControllerBasePath(ClassificationController.class), classification.getId()));
    }

}