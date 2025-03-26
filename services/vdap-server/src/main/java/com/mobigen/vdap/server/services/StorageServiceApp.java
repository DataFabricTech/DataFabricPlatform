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
import com.mobigen.vdap.server.entity.StorageServiceEntity;
import com.mobigen.vdap.server.models.PageModel;
import com.mobigen.vdap.server.repositories.EntityRelationshipRepository;
import com.mobigen.vdap.server.secrets.SecretsManager;
import com.mobigen.vdap.server.tags.TagLabelUtil;
import com.mobigen.vdap.server.util.Fields;
import com.mobigen.vdap.server.util.JsonUtils;
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

import static com.mobigen.vdap.server.Entity.FIELD_OWNERS;

@Slf4j
@Service
public class StorageServiceApp {

    private final static String STORAGE_SERVICE_API_PATH = "/v1/services";

    private final Set<String> allowFields;

    private final TagLabelUtil tagLabelUtil;

    private final StorageServiceRepository storageServiceRepository;
    private final EntityRelationshipRepository entityRelationshipRepository;

    public StorageServiceApp(StorageServiceRepository storageServiceRepository, TagLabelUtil tagLabelUtil, EntityRelationshipRepository entityRelationshipRepository) {
        this.storageServiceRepository = storageServiceRepository;
        this.tagLabelUtil = tagLabelUtil;
        allowFields = Entity.getEntityFields(StorageService.class);
        this.entityRelationshipRepository = entityRelationshipRepository;
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
                predicates.add(criteriaBuilder.equal(root.get("service_type"), serviceType.value()));
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

    public Object list(URI baseUri, ServiceType kindOfService, StorageServiceType serviceType,
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
            addHref(storageService, baseUri);
        });
        res.setTotalElements((int) entities.getTotalElements());
        res.setTotalPages(entities.getTotalPages());
        res.setContents(storageServiceList);
        return res;
    }

    private void setFields(StorageService storageService, Fields fields) {
//        storageService.setOwners(fields.contains(FIELD_OWNERS) ? getOwners(storageService) : null);
//        storageService.setTags(fields.contains(FIELD_TAGS) ? getTags(storageService) : null);
//        storageService.setPipelines(fields.contains(FIELD_PIPELINES) ? getIngestionPipelines(storageService) : null);
    }

    private List<EntityReference> getOwners(StorageService storageService) {
//        List<EntityRelationshipEntity> relationshipEntities =
//                entityRelationshipRepository.findByToIdAndToEntityAndRelation(
//                        storageService.getId().toString(), STORAGE_SERVICE, Relationship.OWNS.ordinal());
        return null;
    }

    @Transactional
    public StorageService create(StorageService entity) {
//        entity = addHref(uriInfo, repository.create(uriInfo, entity));
//        Response.created(entity.getHref()).entity(entity).build();
        prepareInternal(entity);
        entity = createNewEntity(entity);
        return entity;
    }

    protected void prepareInternal(StorageService entity) {
        validateTags(entity);
        prepare(entity);
    }

    protected void validateTags(StorageService entity) {
        validateTags(entity.getTags());
        entity.setTags(tagLabelUtil.addDerivedTags(entity.getTags()));
        tagLabelUtil.checkMutuallyExclusive(entity.getTags());
        tagLabelUtil.checkDisabledTags(entity.getTags());
    }

    protected void validateTags(List<TagLabel> labels) {
        for (TagLabel label : CommonUtil.listOrEmpty(labels)) {
            tagLabelUtil.applyTagCommonFields(label);
        }
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

    protected StorageService createNewEntity(StorageService entity) {
        store(entity, false);
        storeRelationshipsInternal(entity);
        postCreate(entity);
        return entity;
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
        storageServiceRepository.save(storageServiceEntity);

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

        // Restore the relationships
        entity.setOwners(owners);
        entity.setTags(tags);
    }

    protected void storeRelationshipsInternal(StorageService entity) {
        storeOwners(entity, entity.getOwners());
        applyTags(entity);
    }

    protected void postCreate(StorageService entity) {
        // TODO : 검색 엔진 저장
//        searchRepository.createEntity(entity);
    }

    protected void storeOwners(StorageService entity, List<EntityReference> owners) {
        if (!CommonUtil.nullOrEmpty(owners)) {
            for (EntityReference owner : owners) {
                // Add relationship owner --- owns ---> ownedEntity
                log.info("Adding Owner Type[{}] Name[{}] for Entity Type[{}] Id[{}] Name[{}]",
                        owner.getType(), owner.getName(),
                        entity.getServiceType().value(), entity.getId(), entity.getName());
                addRelationship(
                        owner.getId(), entity.getId(), FIELD_OWNERS, Entity.STORAGE_SERVICE, Relationship.OWNS);
            }
        }
    }

    protected void applyTags(StorageService entity) {
        applyTags(entity.getTags(), Entity.STORAGE_SERVICE, entity.getId().toString());
    }

    protected void applyTags(List<TagLabel> tagLabels, String entityType, String targetId) {
        for (TagLabel tagLabel : CommonUtil.listOrEmpty(tagLabels)) {
            if (!tagLabel.getLabelType().equals(TagLabel.LabelType.DERIVED)) {
//                daoCollection
//                        .tagUsageDAO()
//                        .applyTag(
//                                tagLabel.getSource().ordinal(),
//                                tagLabel.getTagFQN(),
//                                tagLabel.getTagFQN(),
//                                targetFQN,
//                                tagLabel.getLabelType().ordinal(),
//                                tagLabel.getState().ordinal());
                log.info("Add Tag Usage Source[{}] ID[{}] Name[{}] LabelType[{}] State[{}] -> Target Entity Type[{}] Id[{}]",
                        tagLabel.getSource().value(), tagLabel.getId().toString(), tagLabel.getName(),
                        tagLabel.getLabelType().value(), tagLabel.getState().value(), entityType, targetId);
            }
        }
    }


    public final void addRelationship(
            UUID fromId, UUID toId, String fromEntity, String toEntity, Relationship relationship) {
        addRelationship(fromId, toId, fromEntity, toEntity, relationship, false);
    }

    public final void addRelationship(
            UUID fromId,
            UUID toId,
            String fromEntity,
            String toEntity,
            Relationship relationship,
            boolean bidirectional) {
        addRelationship(fromId, toId, fromEntity, toEntity, relationship, null, bidirectional);
    }

    public void addRelationship(
            UUID fromId, UUID toId, String fromEntity, String toEntity,
            Relationship relationship, String json, boolean bidirectional) {
        UUID from = fromId;
        UUID to = toId;
        if (bidirectional && fromId.compareTo(toId) > 0) {
            // For bidirectional relationship, instead of adding two row fromId -> toId and toId ->
            // fromId, just add one row where fromId is alphabetically less than toId
            from = toId;
            to = fromId;
        }
        log.info("Insert Relationship : From[{}] - To[{}], FromType[{}] - ToType[{}], RelationType[{}], Json[{}]",
                from, to, fromEntity, toEntity, relationship.value(), json);
//        relationship.
//                .relationshipDAO()
//                .insert(from, to, fromEntity, toEntity, relationship.ordinal(), json);
    }


//    public StorageService createOrUpdate(StorageService request) {
//
//    }

    //    protected void validateTags(StorageService entity) {
//        if (!supportsTags) {
//            return;
//        }
//        validateTags(entity.getTags());
//        entity.setTags(addDerivedTags(entity.getTags()));
//        checkMutuallyExclusive(entity.getTags());
//        checkDisabledTags(entity.getTags());
//    }
//    protected void validateTags(List<TagLabel> labels) {
//        for (TagLabel label : listOrEmpty(labels)) {
//            TagLabelUtil.applyTagCommonFields(label);
//        }
//    }

    private StorageService convertToDto(StorageServiceEntity entity) {
        return JsonUtils.readValue(entity.getJson(), StorageService.class);
    }

    private void addHref(StorageService entity, URI baseUri) {
        // TODO : Add Hred ( Owner, Tag, Pipeline )
        entity.setHref(URI.create(
                String.format("%s%s/%s", baseUri.toString(),
                        STORAGE_SERVICE_API_PATH,
                        entity.getId().toString())));
    }


}
