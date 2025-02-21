package com.mobigen.vdap.server.services;

import com.mobigen.vdap.common.utils.CommonUtil;
import com.mobigen.vdap.schema.entity.services.StorageService;
import com.mobigen.vdap.schema.type.EntityReference;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.schema.type.TagLabel;
import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.secrets.SecretsManager;
import com.mobigen.vdap.server.tags.TagLabelUtil;
import com.mobigen.vdap.server.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.mobigen.vdap.server.entity.StorageServiceEntity;

@Slf4j
@Service
public class StorageServiceApp {

    private final StorageServiceRepository repository;
    private final TagLabelUtil tagLabelUtil;

    public StorageServiceApp(StorageServiceRepository repository, TagLabelUtil tagLabelUtil) {
        this.repository = repository;
        this.tagLabelUtil = tagLabelUtil;
    }

    public void prepareInternal(StorageService entity) {
//        validateTags(entity);
        prepare(entity);
    }

    protected void validateTags(StorageService entity) {
        validateTags(entity.getTags());
        // TODO : tag 처리 해야 함.
//        entity.setTags(addDerivedTags(entity.getTags()));
//        checkMutuallyExclusive(entity.getTags());
//        checkDisabledTags(entity.getTags());
    }

    protected void validateTags(List<TagLabel> labels) {
        for (TagLabel label : CommonUtil.listOrEmpty(labels)) {
            tagLabelUtil.applyTagCommonFields(label);
        }
    }

    public void prepare(StorageService service) {
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

    @Transactional
    public StorageService create(StorageService entity) {
//        entity = addHref(uriInfo, repository.create(uriInfo, entity));
//        Response.created(entity.getHref()).entity(entity).build();
        prepareInternal(entity);
        return createNewEntity(entity);
    }

    protected StorageService createNewEntity(StorageService entity) {
        store(entity, false);
        storeRelationshipsInternal(entity);
        postCreate(entity);
        return entity;
    }

    protected void store(StorageService entity, boolean update) {
        // Don't store owner, database, href and tags as JSON. Build it on the fly based on
        // relationships
        List<EntityReference> owners = entity.getOwners();
        entity.setOwners(null);
        List<TagLabel> tags = entity.getTags();
        entity.setTags(null);

        StorageServiceEntity storageServiceEntity = StorageServiceEntity.builder()
                .id(entity.getId().toString())
                .kind(entity.getKindOfService())
                .serviceType(entity.getServiceType().value())
                .json(JsonUtils.pojoToJson(entity))
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
        repository.save(storageServiceEntity);

        if (update) {
            // Update the entity
            log.info("Updated Kind{} Type{} Id{} Name{} Display{}",
                    entity.getKindOfService().value(), entity.getServiceType().value(),
                    entity.getId(), entity.getName(), entity.getDisplayName());
        } else {
            // insert the entity
            log.info("Insert Kind{} Type{} Id{} Name{} Display{}",
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
                        owner.getId(), entity.getId(), Entity.FIELD_OWNERS, Entity.STORAGE_SERVICE, Relationship.OWNS);
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
}
