package com.mobigen.vdap.server.services;

import com.github.fge.jsonpatch.JsonPatch;
import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.services.ServiceType;
import com.mobigen.vdap.schema.entity.services.StorageService;
import com.mobigen.vdap.schema.entity.services.StorageService.StorageServiceType;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.Include;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.schema.type.TagLabel;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.RelationshipEntity;
import com.mobigen.vdap.server.entity.StorageServiceEntity;
import com.mobigen.vdap.server.entity.TagUsageEntity;
import com.mobigen.vdap.server.exception.CustomException;
import com.mobigen.vdap.server.extensions.ExtensionService;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.relationship.RelationshipService;
import com.mobigen.vdap.server.relationship.TagUsageService;
import com.mobigen.vdap.server.secrets.SecretsManager;
import com.mobigen.vdap.server.tags.TagController;
import com.mobigen.vdap.server.tags.TagLabelUtil;
import com.mobigen.vdap.server.users.UserService;
import com.mobigen.vdap.server.util.*;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static com.mobigen.vdap.server.util.EntityUtil.compareTagLabel;

@Slf4j
@Service
public class StorageServiceApp {

    private final Set<String> allowFields;
    private final StorageServiceRepository storageServiceRepository;

    private final UserService userService;
    private final TagLabelUtil tagLabelUtil;
    private final TagUsageService tagUsageService;
    private final RelationshipService relationshipService;
    private final ExtensionService extensionService;

    public StorageServiceApp(StorageServiceRepository storageServiceRepository,
                             UserService userService, TagLabelUtil tagLabelUtil, TagUsageService tagUsageService,
                             RelationshipService relationshipService, ExtensionService extensionService) {
        this.storageServiceRepository = storageServiceRepository;
        this.tagLabelUtil = tagLabelUtil;
        this.userService = userService;
        this.tagUsageService = tagUsageService;
        this.relationshipService = relationshipService;
        this.extensionService = extensionService;
        allowFields = Entity.getEntityFields(StorageService.class);
    }

    private Fields getFields(String fields) {
        if ("*".equals(fields)) {
            return new Fields(allowFields, String.join(",", allowFields));
        }
        return new Fields(allowFields, fields);
    }

    private Specification<StorageServiceEntity> withDynamicConditions(
            String id, String name, ServiceType kindOfService, StorageServiceType serviceType, Include include) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), id));
            }
            if (name != null) {
                predicates.add(criteriaBuilder.equal(root.get("name"), name));
            }
            // kindOfService가 null이 아닌 경우에만 조건 추가
            if (kindOfService != null) {
                predicates.add(criteriaBuilder.equal(root.get("kind"), kindOfService.toString().toLowerCase()));
            }

            // serviceType이 null이 아닌 경우에만 조건 추가
            if (serviceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceType"), serviceType.toString().toLowerCase()));
            }

            if (include != null) {
                if (include.equals(Include.DELETED)) {
                    predicates.add(criteriaBuilder.equal(root.get("deleted"), true));
                } else if (include.equals(Include.NON_DELETED)) {
                    predicates.add(criteriaBuilder.or(criteriaBuilder.isNull(root.get("deleted")),
                            criteriaBuilder.equal(root.get("deleted"), false)));
                }
            }

            // 조건이 없으면 항상 true를 반환하여 모든 데이터 조회
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public PageModel<StorageService> list(URI baseUri, ServiceType kindOfService, StorageServiceType serviceType,
                                          String fieldsParam, Integer page, Integer size, Include include) {
        // 사용자가 요청한 추가 정보 확인
        Fields fields = getFields(fieldsParam);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));

        Page<StorageServiceEntity> entities = storageServiceRepository.findAll(
                withDynamicConditions(null, null, kindOfService, serviceType, include), pageable);
        PageModel<StorageService> res = new PageModel<>();
        res.setPage(page);
        res.setSize(size);
        if (entities.getTotalElements() <= 0) {
            log.info("[StorageService] List Result : Not Have StorageService");
            res.setTotalElements(0);
            res.setTotalPages(0);
            res.setContents(Collections.emptyList());
            return res;
        }
        log.info("[StorageService] List Result : Page[{}/{}] TotalElements[{}]",
                pageable.getPageNumber(), pageable.getPageSize(), entities.getTotalElements());
        // Convert : Entity -> DTO
        List<StorageService> storageServiceList = entities.getContent().stream().map(this::convertToDto).toList();
        // SetFields
        storageServiceList.forEach(storageService -> {
            setFields(storageService, fields);
            addHref(baseUri, storageService);
        });
        res.setTotalElements((int) entities.getTotalElements());
        res.setTotalPages(entities.getTotalPages());
        res.setContents(storageServiceList);
        return res;
    }

    public StorageService getById(URI baseUri, UUID id, String fieldsParam, Include include) {
        StorageServiceEntity entity = storageServiceRepository.findOne(
                withDynamicConditions(String.valueOf(id), null, null, null, include)).orElse(null);
        if (entity == null) {
            log.error("[StorageService] Not Found By Id[{}]", id.toString());
            throw new CustomException("[StorageService] Not Found By Id", id.toString());
        }
        return getInternal(baseUri, entity, fieldsParam);
    }

    public StorageService getByName(URI baseUri, String name, String fieldsParam, Include include) {
        StorageServiceEntity entity = storageServiceRepository.findOne(
                withDynamicConditions(null, name, null, null, include)).orElse(null);
        if (entity == null) {
            log.error("[StorageService] Not Found By Name[{}]", name);
            throw new CustomException("[StorageService] Not Found By Name", name);
        }
        return getInternal(baseUri, entity, fieldsParam);
    }

    public StorageService getInternal(URI baseUri, StorageServiceEntity entity, String fieldsParam) {
        // Entity -> DTO
        StorageService storageService = convertToDto(entity);
        // 사용자가 요청한 추가 정보 확인
        Fields fields = getFields(fieldsParam);
        // 필드 채우기
        setFields(storageService, fields);
        // 링크 추가
        addHref(baseUri, storageService);
        return storageService;
    }

    private void setFields(StorageService storageService, Fields fields) {
        storageService.setOwners(fields.contains(Entity.FIELD_OWNERS) ? getOwners(storageService) : null);
        storageService.setTags(fields.contains(Entity.FIELD_TAGS) ? getTags(storageService) : null);
        // TODO : Pipeline 정보 추가
        // storageService.setPipelines(fields.contains(FIELD_PIPELINES) ? getIngestionPipelines(storageService) : null);
    }

    private List<EntityReference> getOwners(StorageService storageService) {
        List<RelationshipEntity> relations =
                relationshipService.getRelationships(null, storageService.getId(),
                        null, Entity.STORAGE_SERVICE, Relationship.OWNS, null);
        return userService.getReferences(relations);
    }

    private List<TagLabel> getTags(StorageService storageService) {
        List<TagUsageEntity> usages =
                tagUsageService.getTagUsages(null, null, null, Entity.STORAGE_SERVICE, storageService.getId().toString());
        return tagLabelUtil.getTagLabels(usages);
    }

    @Transactional
    public StorageService create(URI baseUri, StorageService entity) {
        // 검증 및 저장 준비
        prepareInternal(entity);
        // 데이터 저장
        store(entity, false);
        // 관계(소유자, 태그, ) 저장
        storeRelationships(entity);
        // postCreate(entity) // 데이터 저장소는 검색 대상이 아님
        addHref(baseUri, entity);
        return entity;
    }

    private void prepareInternal(StorageService entity) {
        // TODO : Owner 유효성 검사
//        validateOwner(entity);
        // TagLabel 유효성 검사
        validateTags(entity);
        // Connection Config 의 Password 필드를 암호화 한다.
        prepare(entity);
    }

    private void validateTags(StorageService entity) {
        // TagLabel 유효성 검사와 기타 정보를 원본(Tag, GlossaryTerm)에서 가져온다.
        for (TagLabel label : CommonUtil.listOrEmpty(entity.getTags())) {
            tagLabelUtil.applyTagCommonFields(label);
        }
        // 중복 제거와 정렬
        List<TagLabel> uniqueSortedTags = entity.getTags().stream()
                .distinct()
                .sorted(compareTagLabel)
                .toList();
        entity.setTags(new ArrayList<>(uniqueSortedTags));
        // 배타적 태그가 설정되었는지 확인
        tagLabelUtil.checkMutuallyExclusive(entity.getTags());
        // 비활성화된 태그가 설정되었는지 확인
//        tagLabelUtil.checkDisabledTags(entity.getTags());
    }

    private void prepare(StorageService service) {
        if (service.getConnection() != null) {
            // Connection Config 의 Password 필드를 암호화 한다.
            service
                    .getConnection()
                    .setConfig(
                            new SecretsManager()
                                    .encryptServiceConnectionConfig(
                                            service.getConnection().getConfig(),
                                            service.getServiceType().value(),
                                            service.getName(),
                                            service.getKindOfService()));
        }
    }

    private void store(StorageService entity, boolean update) {
        // 데이터 저장소 가상화를 위한 데이터 중 소유자 정보와 태그 정보는 별도로 저장한다.
        List<EntityReference> owners = entity.getOwners();
        entity.setOwners(null);
        List<TagLabel> tags = entity.getTags();
        entity.setTags(null);

        StorageServiceEntity storageServiceEntity = StorageServiceEntity.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .kind(entity.getKindOfService().value())
                .serviceType(entity.getServiceType().value())
                .json(JsonUtils.pojoToJson(entity))
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .deleted(entity.getDeleted())
                .build();

        if (update) {
            // Update the entity
            log.info("Update Kind[{}] Type[{}] Id[{}] Name[{}] DisplayName[{}] Connection[{}]",
                    entity.getKindOfService().value(), entity.getServiceType().value(),
                    entity.getId(), entity.getName(), entity.getDisplayName(), entity.getConnection().getConfig());
        } else {
            // insert the entity
            log.info("Insert Kind[{}] Type[{}] Id[{}] Name[{}] DisplayName[{}]",
                    entity.getKindOfService().value(), entity.getServiceType().value(),
                    entity.getId(), entity.getName(), entity.getDisplayName());
        }
        storageServiceRepository.save(storageServiceEntity);

        // Restore the relationships
        entity.setOwners(owners);
        entity.setTags(tags);
    }

    private void storeRelationships(StorageService entity) {
        storeOwners(entity);
        applyTags(entity);
    }

    private void storeOwners(StorageService entity) {
        if (!CommonUtil.nullOrEmpty(entity.getOwners())) {
            for (EntityReference owner : entity.getOwners()) {
                // Add relationship owner --- owns ---> ownedEntity
                log.info("[StorageService] Insert Relationship From - Type[{}] ID[{}] Name[{}]  To - Owner ID[{}] Name[{}]",
                        entity.getServiceType(), entity.getId(), entity.getName(), owner.getId(), owner.getName());
                relationshipService.addRelationship(
                        owner.getId(), entity.getId(), Entity.USER, Entity.STORAGE_SERVICE, Relationship.OWNS);
            }
        }
    }

    private void applyTags(StorageService entity) {
        log.info("[StorageService] Apply Tags : ID[{}] Name[{}] TagsCount[{}]",
                entity.getId(), entity.getName(), CommonUtil.listOrEmpty(entity.getTags()).size());
        tagUsageService.applyTags(entity.getTags(), Entity.STORAGE_SERVICE, entity.getId().toString());
    }

    @Transactional
    public StorageService update(URI baseUri, StorageService updated) {
        // Get Original Entity
        StorageServiceEntity originalEntity = storageServiceRepository.findOne(
                withDynamicConditions(String.valueOf(updated.getId()), null, null, null, null)).orElse(null);
        if (originalEntity == null) {
            log.error("[StorageService] Can Not Update. Not Found By Id[{}]", updated.getId().toString());
            throw new CustomException("[StorageService] Can Not Update. Not Found By Id", updated.getId());
        }
        // 업데이트 데이터 검증
        prepareInternal(updated);
        // Entity -> DTO
        StorageService originalService = convertToDto(originalEntity);
        // PipeLine 을 제외한 모든 필드 정보 로드
        setFields(originalService, getFields("*"));
        // 데이터 비교와 저장
        updateInternal(originalService, updated);
        addHref(baseUri, updated);
        return updated;
    }

    @Transactional
    public StorageService patch(URI baseUri, UUID id, JsonPatch patch, String updatedBy) {
        // Get Original Entity
        StorageServiceEntity originalEntity = storageServiceRepository.findOne(
                withDynamicConditions(id.toString(), null, null, null, null)).orElse(null);
        if (originalEntity == null) {
            log.error("[StorageService] Can Not Patch. Not Found By Id[{}]", id);
            throw new CustomException("[StorageService] Can Not Patch. Not Found By Id", id);
        }
        StorageService originalService = convertToDto(originalEntity);
        // PipeLine 을 제외한 모든 필드 정보 로드
        setFields(originalService, getFields("*"));
        // Patch 를 적용하여 Update 데이터를 생성
        StorageService updated = JsonUtils.applyJsonPatch(originalService, patch, StorageService.class);
        // 데이터 비교와 저장
        updateInternal(originalService, updated);
        addHref(baseUri, updated);
        return updated;
    }

    private void updateInternal(StorageService original, StorageService updated) {
        /* 변경 불가 정보 복사 */
        updated.setId(original.getId());
        updated.setKindOfService(original.getKindOfService());
        updated.setServiceType(original.getServiceType());

        /* CreaetStorageService 데이터를 활용하는 관계로 다음 데이터에 대한 데이터만 업데이트가 가능함. */
        /* name, displayName, description, connection, testConnectionResult, tags, owners */

        /*
         * Storage Service {
         * // 불변
         * id, kindOfService, serviceType,
         * // 변경 가능(비교 대상)
         * name, displayName, description, connection, pipelines,
         * testConnectionResult, tags, owners, updatedBy, deleted
         * // 비교 대상이 아님
         * version, updatedAt, changeDescription, href,
         * }
         */

        // 비교 대상이 아닌 데이터는 null 처리
        // TODO : pipeline 은 추후
        updated.withPipelines(null);
        original.withPipelines(null);

        Double orgVersion = original.getVersion();
        original.setVersion(null);
        updated.setVersion(null);

        LocalDateTime orgUpdatedAt = original.getUpdatedAt();
        LocalDateTime updatedAt = updated.getUpdatedAt();
        original.setUpdatedAt(null);
        updated.setUpdatedAt(null);

        String orgChangeDescription = original.getChangeDescription();
        original.setChangeDescription(null);
        updated.setChangeDescription(null);

        original.setHref(null);
        updated.setHref(null);

        String patch = JsonUtils.pojoToJson(JsonUtils.getJsonPatch(original, updated));
        log.debug("[StorageService] Update Storage Service ID[{}] Name[{}] JsonPatch - \n{}",
                original.getId().toString(), original.getName(), patch);

        // 저장했던 데이터 복구
        updated.setVersion(EntityUtil.nextVersion(orgVersion));
        updated.setUpdatedAt(updatedAt);
        updated.setChangeDescription(patch);
        // 저장
        store(updated, true);
        updateRelationships(updated);

        // 기존 데이터 복구 후 히스토리에 저장
        original.setVersion(orgVersion);
        original.setUpdatedAt(orgUpdatedAt);
        original.setChangeDescription(orgChangeDescription);
        storeEntityHistory(original);
    }

    private void updateRelationships(StorageService entity) {
        updateOwners(entity);
        updateTags(entity);
    }

    private void updateOwners(StorageService entity) {
        // 기존 소유자 삭제
        relationshipService.deleteRelationship(null, entity.getId(), Entity.USER, Entity.STORAGE_SERVICE, Relationship.OWNS);
        storeOwners(entity);
    }

    private void updateTags(StorageService entity) {
        // 기존 태그 삭제
        tagUsageService.delete(null, null, null, Entity.STORAGE_SERVICE, entity.getId().toString());
        applyTags(entity);
    }

    private void storeEntityHistory(StorageService storageService) {
        String extensionName = EntityUtil.getVersionExtension(Entity.STORAGE_SERVICE, storageService.getVersion());
        log.info("[StorageService] ID[{}] Name[{}] Save Extension Name(Version)[{}]",
                storageService.getId().toString(), storageService.getName(), extensionName);
        extensionService.addExtension(storageService.getId().toString(), extensionName, Entity.STORAGE_SERVICE, storageService);
    }

    private StorageService convertToDto(StorageServiceEntity entity) {
        return JsonUtils.readValue(entity.getJson(), StorageService.class);
    }

    private void addHref(URI baseUri, StorageService entity) {
        // TODO : Add Href ( Pipeline )
        for (EntityReference owner : CommonUtil.listOrEmpty(entity.getOwners())) {
            owner.withHref(RestUtil.getHref(baseUri, "/v1/users", owner.getId()));
        }
        for (TagLabel tag : CommonUtil.listOrEmpty(entity.getTags())) {
            if (tag.getSource().equals(TagLabel.TagSource.CLASSIFICATION)) {
                tag.withHref(RestUtil.getHref(baseUri, RestUtil.getControllerBasePath(TagController.class), tag.getId()));
            }
//            else {
//                tag.withHref(RestUtil.getHref(baseUri, RestUtil.getControllerBasePath(GlossaryTerm.class), tag.getId()));
//            }
        }
        entity.withHref(RestUtil.getHref(baseUri, RestUtil.getControllerBasePath(StorageServiceController.class), entity.getId()));
    }

    @Transactional
    public void deleteById(UUID id, boolean recursive, boolean hardDelete, String deletedBy) {
        StorageServiceEntity entity = storageServiceRepository.findOne(
                withDynamicConditions(id.toString(), null, null, null, null)).orElse(null);
        if (entity == null) {
            log.error("[StorageService] Can Not Delete. Not Found By Id[{}]", id);
            throw new CustomException("[StorageService] Can Not Delete. Not Found By Id", id.toString());
        }
        deleteInternal(entity, recursive, hardDelete, deletedBy);
    }

    @Transactional
    public void deleteByName(String name, boolean recursive, boolean hardDelete, String deletedBy) {
        StorageServiceEntity entity = storageServiceRepository.findOne(
                withDynamicConditions(null, name, null, null, null)).orElse(null);
        if (entity == null) {
            log.error("[StorageService] Can Not Delete. Not Found By Name[{}]", name);
            throw new CustomException("[StorageService] Can Not Delete. Not Found By Name", name);
        }
        deleteInternal(entity, recursive, hardDelete, deletedBy);
    }

    private void deleteInternal(StorageServiceEntity entity, boolean recursive, boolean hardDelete, String deletedBy) {
        log.info("[StorageService] ID[{}] Name[{}] {} Deleted", entity.getId(), entity.getName(), hardDelete ? "Hard" : "Soft");
        deleteChildren(entity.getId(), recursive, hardDelete, deletedBy);
        StorageService original = convertToDto(entity);
        setFields(original, getFields("*"));
        StorageService updated = JsonUtils.deepCopy(original, StorageService.class);
        if (!hardDelete) {
            updated.setUpdatedBy(deletedBy);
            updated.setUpdatedAt(Utilities.getLocalDateTime());
            updated.setDeleted(true);
            updateInternal(original, updated);
        } else {
            cleanup(updated);
        }
    }

    private void deleteChildren(String id, boolean recursive, boolean hardDelete, String deletedBy) {
        List<RelationshipEntity> childrenRecords =
                relationshipService.getRelationships(
                        UUID.fromString(id), null, Entity.STORAGE_SERVICE,
                        null, List.of(Relationship.CONTAINS, Relationship.PARENT_OF), null);

        if (childrenRecords.isEmpty()) {
            log.info("[StorageService] No Have children. ID[{}]", id);
            return;
        }
        if (!recursive) {
            log.warn("[StorageService] Can Not Delete Storage Service[{}]. Have A Children Len[{}]", id, childrenRecords.size());
            throw new CustomException("[StorageService] Can Not Delete Storage Service. Have A Children", id.toString());
        }
        deleteChildren(childrenRecords, hardDelete, deletedBy);
    }

    private void deleteChildren(
            List<RelationshipEntity> children, boolean hardDelete, String updatedBy) {
        for (RelationshipEntity relationship : children) {
            log.info("[StorageService] Recursively {} deleting {} {}", hardDelete ? "hard" : "soft", relationship.getToEntity(), relationship.getToId());
            switch (relationship.getToEntity()) {
                case Entity.DATABASE -> {
                    // TODO : Database Delete
                }
                case Entity.BUCKET -> {
                    // TODO : Bucket Delete
                }
                case Entity.INGESTION_PIPELINE -> {
                    // TODO : Ingestion Pipeline Delete
                }
                default -> {
                    log.warn("[StorageService] Unsupported Child Entity Type[{}]", relationship.getToEntity());
                }
            }
        }
    }

    private void cleanup(StorageService storageService) {
        UUID id = storageService.getId();

        // 연관관계 정보 테이블에서 삭제
        log.info("[StorageService] ID[{}] Name[{}] Delete Relationship Data By From",
                storageService.getId().toString(), storageService.getName());
        relationshipService.deleteRelationship(id, null,
                Entity.STORAGE_SERVICE, null, null);
        log.info("[StorageService] ID[{}] Name[{}] Delete Relationship Data By To",
                storageService.getId().toString(), storageService.getName());
        relationshipService.deleteRelationship(null, id,
                null, Entity.STORAGE_SERVICE, null);

        // Delete all the tag labels
        log.info("[StorageService] ID[{}] Name[{}] Delete TagUsage. By TargetEntity",
                storageService.getId(), storageService.getName());
        tagUsageService.delete(null, null, null,
                Entity.STORAGE_SERVICE, storageService.getId().toString());

        // Delete All the extensions of entity
        log.info("[StorageService] ID[{}] Name[{}] Delete Extension Data By ID",
                storageService.getId(), storageService.getName());
        String versionPrefix = EntityUtil.getVersionExtensionPrefix(Entity.STORAGE_SERVICE) + ".";
        extensionService.deleteExtensions(storageService.getId().toString(), versionPrefix);

        // Finally, delete the entity
        log.info("[StorageService] Id[{}] Name[{}] Finally Delete",
                storageService.getId().toString(), storageService.getName());
        storageServiceRepository.deleteById(id.toString());
    }

}
