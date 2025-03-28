package com.mobigen.vdap.server.services;

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
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.relationship.RelationshipService;
import com.mobigen.vdap.server.relationship.TagUsageRepository;
import com.mobigen.vdap.server.relationship.TagUsageService;
import com.mobigen.vdap.server.secrets.SecretsManager;
import com.mobigen.vdap.server.tags.TagController;
import com.mobigen.vdap.server.tags.TagLabelUtil;
import com.mobigen.vdap.server.users.UserService;
import com.mobigen.vdap.server.util.Fields;
import com.mobigen.vdap.server.util.JsonUtils;
import com.mobigen.vdap.server.util.RestUtil;
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

    public StorageServiceApp(StorageServiceRepository storageServiceRepository,
                             UserService userService, TagLabelUtil tagLabelUtil, TagUsageService tagUsageService,
                             RelationshipService relationshipService) {
        this.storageServiceRepository = storageServiceRepository;
        this.tagLabelUtil = tagLabelUtil;
        this.userService = userService;
        this.tagUsageService = tagUsageService;
        this.relationshipService = relationshipService;
        allowFields = Entity.getEntityFields(StorageService.class);
    }

    private Fields getFields(String fields) {
        if ("*".equals(fields)) {
            return new Fields(allowFields, String.join(",", allowFields));
        }
        return new Fields(allowFields, fields);
    }

    private Specification<StorageServiceEntity> withDynamicConditions(
            ServiceType kindOfService, StorageServiceType serviceType, Include include) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // kindOfService가 null이 아닌 경우에만 조건 추가
            if (kindOfService != null) {
                predicates.add(criteriaBuilder.equal(root.get("kind"), kindOfService.value()));
            }

            // serviceType이 null이 아닌 경우에만 조건 추가
            if (serviceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceType"), serviceType.value()));
            }

            if (include != null && include != Include.ALL) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), include.equals(Include.DELETED)));
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
                withDynamicConditions(kindOfService, serviceType, include), pageable);
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
        StorageServiceEntity entity = storageServiceRepository.findById(id.toString()).orElse(null);
        if (entity == null) {
            throw new CustomException("[StorageService] Not Found By Id", id.toString());
        }
        if (include != null && include.equals(Include.DELETED)) {
            if (!entity.getDeleted()) {
                throw new CustomException(
                        String.format("[StorageService] Not Found By Id[%s] Include[%s]",
                                id, include), null);
            }
        }
        return getInternal(baseUri, entity, fieldsParam);
    }

    public StorageService getByName(URI baseUri, String name, String fieldsParam, Include include) {
        StorageServiceEntity entity = storageServiceRepository.findByName(name).orElse(null);
        if (entity == null) {
            throw new CustomException("[StorageService] Not Found By Name", name);
        }
        if (include != null && include.equals(Include.DELETED)) {
            if (!entity.getDeleted()) {
                throw new CustomException(
                        String.format("[StorageService] Not Found By Name[%s] Include[%s]", name, include), null);
            }
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

    protected void prepareInternal(StorageService entity) {
        // TagLabel 유효성 검사
        validateTags(entity);
        // Connection Config 의 Password 필드를 암호화 한다.
        prepare(entity);
    }

    protected void validateTags(StorageService entity) {
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

    protected void prepare(StorageService service) {
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

    protected void store(StorageService entity, boolean update) {
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

    protected void storeRelationships(StorageService entity) {
        storeOwners(entity);
        applyTags(entity);
    }

    protected void storeOwners(StorageService entity) {
        if (!CommonUtil.nullOrEmpty(entity.getOwners())) {
            for (EntityReference owner : entity.getOwners()) {
                // Add relationship owner --- owns ---> ownedEntity
                log.info("[StorageService] Insert Relationship From - Type[{}] ID[{}] Name[{}]  To - Owner ID[{}] Name[{}]",
                        entity.getServiceType(), entity.getId(), entity.getName(), owner.getId(), owner.getName());
                relationshipService.addRelationship(
                        owner.getId(), entity.getId(), Entity.OWNER, Entity.STORAGE_SERVICE, Relationship.OWNS);
            }
        }
    }

    protected void applyTags(StorageService entity) {
        log.info("[StorageService] Apply Tags : ID[{}] Name[{}] TagsCount[{}]",
                entity.getId(), entity.getName(), CommonUtil.listOrEmpty(entity.getTags()).size());
        tagUsageService.applyTags(entity.getTags(), Entity.STORAGE_SERVICE, entity.getId().toString());
    }

//    public StorageService createOrUpdate(StorageService request) {
//
//    }

    private StorageService convertToDto(StorageServiceEntity entity) {
        return JsonUtils.readValue(entity.getJson(), StorageService.class);
    }

    private void addHref(URI baseUri, StorageService entity) {
        // TODO : Add Href ( Pipeline )
        entity.getOwners().forEach(owner -> {
            owner.withHref(RestUtil.getHref(baseUri, "/v1/users", owner.getId()));
        });
        entity.getTags().forEach(tag -> {
            if (tag.getSource().equals(TagLabel.TagSource.CLASSIFICATION)) {
                tag.withHref(RestUtil.getHref(baseUri, RestUtil.getControllerBasePath(TagController.class), tag.getId()));
            }
//            else {
//                tag.withHref(RestUtil.getHref(baseUri, RestUtil.getControllerBasePath(GlossaryTerm.class), tag.getId()));
//            }
        });
        entity.withHref(RestUtil.getHref(baseUri, RestUtil.getControllerBasePath(StorageServiceController.class), entity.getId()));
    }


}
