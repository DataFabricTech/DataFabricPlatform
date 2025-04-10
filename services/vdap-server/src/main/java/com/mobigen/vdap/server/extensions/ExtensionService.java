package com.mobigen.vdap.server.extensions;

import com.mobigen.vdap.server.Entity;
import com.mobigen.vdap.server.entity.EntityExtension;
import com.mobigen.vdap.server.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExtensionService {

    private final ExtensionRepository extensionRepository;

    public ExtensionService(ExtensionRepository extensionRepository) {
        this.extensionRepository = extensionRepository;
    }

    public void addExtension(String id, String extension, String dataType, Object data) {
        log.info("[Extension] ID[{}] Extension[{}] DataType[{}]", id, extension, dataType);
        EntityExtension entityExtension = EntityExtension.builder()
                .id(id)
                .extension(extension)
                .entityType(dataType)
                .json(JsonUtils.pojoToJson(data)).build();
        extensionRepository.save(entityExtension);
    }

    public static Specification<EntityExtension> hasId(String id) {
        return (root, query, criteriaBuilder) ->
                id == null ? null : criteriaBuilder.equal(root.get("id"), id);
    }

    public static Specification<EntityExtension> hasExtension(String extension) {
        return (root, query, criteriaBuilder) ->
                extension == null ? null : criteriaBuilder.equal(root.get("extension"), extension);
    }

    public static Specification<EntityExtension> hasExtensionLike(String extension) {
        return (root, query, criteriaBuilder) ->
                extension == null ? null : criteriaBuilder.like(root.get("extension"), extension + "%");
    }

    public EntityExtension getExtension(String id, String extension) {
        log.info("[Extension] Get Extension : Id[{}] - Extension[{}]", id, extension);
        Specification<EntityExtension> spec =
                Specification.where(hasId(id))
                        .and(hasExtension(extension));
        return extensionRepository.findOne(spec).orElse(null);
    }

//    public EntityExtension getExtension(String id, String extension, String entityType) {
//        log.info("[Extension] Get Extension : Id[{}] - Extension[{}] - EntityType[{}]", id, extension, entityType);
//        Specification<EntityExtension> spec =
//                Specification.where(hasId(id))
//                        .and(hasExtension(extension))
//                        .and(hasEntityType(entityType));
//        return extensionRepository.findOne(spec).orElse(null);
//    }

    public List<EntityExtension> getExtensions(String id, String extensionPrefix) {
        log.info("[Extension] Get Extensions : Id[{}] - ExtensionPrefix[{}]", id, extensionPrefix);
        Specification<EntityExtension> spec =
                Specification.where(hasId(id))
                        .and(hasExtensionLike(extensionPrefix));
        return extensionRepository.findAll(spec, Sort.by(Sort.Order.desc(Entity.FIELD_EXTENSION)));
    }

    public void deleteExtensions(
            String id, String extensionPrefix) {
        log.info("[Extension] Delete Extension : ID[{}] Extension[{}]", id, extensionPrefix);
        Specification<EntityExtension> spec =
                Specification.where(hasId(id))
                        .and(hasExtensionLike(extensionPrefix));
        extensionRepository.delete(spec);
    }
}
