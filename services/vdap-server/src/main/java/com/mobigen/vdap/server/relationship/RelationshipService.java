package com.mobigen.vdap.server.relationship;

import com.mobigen.vdap.schema.type.Include;
import com.mobigen.vdap.schema.type.Relationship;
import com.mobigen.vdap.server.entity.RelationshipEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    public RelationshipService(RelationshipRepository relationshipRepository) {
        this.relationshipRepository = relationshipRepository;
    }

    public void addRelationship(
            UUID fromId, UUID toId, String fromEntity, String toEntity, Relationship relationship) {
        addRelationship(fromId, toId, fromEntity, toEntity, relationship, false);
    }

    public void addRelationship(
            UUID fromId, UUID toId,
            String fromEntity, String toEntity,
            Relationship relationship, boolean bidirectional) {
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
        RelationshipEntity entity = new RelationshipEntity();
        entity.setFromId(from.toString());
        entity.setToId(to.toString());
        entity.setFromEntity(fromEntity);
        entity.setToEntity(toEntity);
        entity.setRelation(relationship.ordinal());
        entity.setJsonSchema(null);
        entity.setJson(json);
        entity.setDeleted(false);

        log.info("[Relationship] Insert Relationship : From[{}/{}] - Relation[{}/{}] - To[{}/{}]",
                fromEntity, from, relationship, relationship.ordinal(), toEntity, to);

        relationshipRepository.save(entity);
    }

    private Specification<RelationshipEntity> withDynamicConditions(
            UUID fromId, UUID toId, String fromEntity, String toEntity,
            Relationship relation, Include include) {
        return withDynamicConditions(
                fromId, toId, fromEntity, toEntity,
                relation != null ? Collections.singletonList(relation) : null,
                include
        );
    }

    private Specification<RelationshipEntity> withDynamicConditions(
            UUID fromId, UUID toId, String fromEntity, String toEntity, List<Relationship> relation, Include include) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // !!!! criteriaBuilder 는 실제 컬럼 명이 아닌 entity 명을 사용해야 함
            if (fromId != null) {
                predicates.add(criteriaBuilder.equal(root.get("fromId"), fromId.toString()));
            }
            if (fromEntity != null) {
                predicates.add(criteriaBuilder.equal(root.get("fromEntity"), fromEntity));
            }
            if (toId != null) {
                predicates.add(criteriaBuilder.equal(root.get("toId"), toId.toString()));
            }
            if (toEntity != null) {
                predicates.add(criteriaBuilder.equal(root.get("toEntity"), toEntity));
            }
            if (relation != null && !relation.isEmpty()) {
                List<Integer> relationCodes = relation.stream()
                        .map(Relationship::ordinal)
                        .collect(Collectors.toList());
                predicates.add(root.get("relation").in(relationCodes));
            }

            if (include == null || include.equals(Include.NON_DELETED)) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
            }
            if (include != null && include.equals(Include.DELETED)) {
                predicates.add(criteriaBuilder.equal(root.get("deleted"), true));
            }

            // 조건이 없으면 항상 true를 반환하여 모든 데이터 조회
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public List<RelationshipEntity> getRelationships(
            UUID fromId, UUID toId, String fromEntity, String toEntity,
            List<Relationship> relationship, @Nullable Include include) {
        log.info("[Relationship] Get Relationships : From[{}/{}] - Relation[{}] - To[{}/{}] - Include[{}]",
                fromEntity, fromId, relationship, toEntity, toId, include);
        return relationshipRepository.findAll(
                withDynamicConditions(fromId, toId, fromEntity, toEntity,
                        relationship, include));
    }

    public List<RelationshipEntity> getRelationships(
            UUID fromId, UUID toId, String fromEntity, String toEntity,
            Relationship relationship, @Nullable Include include) {
        log.info("[Relationship] Get Relationships : From[{}/{}] - Relation[{}] - To[{}/{}] - Include[{}]",
                fromEntity, fromId, relationship, toEntity, toId, include);
        return relationshipRepository.findAll(
                withDynamicConditions(fromId, toId, fromEntity, toEntity,
                        relationship, include));
    }

    public void deleteRelationship(
            UUID fromId, UUID toId, String fromEntity, String toEntity,
            @Nullable Relationship relationship) {
        log.info("[Relationship] Delete Relationships : From[{}/{}] - Relation[{}] - To[{}/{}]",
                fromEntity, fromId, relationship, toEntity, toId);
        relationshipRepository.delete(withDynamicConditions(fromId, toId, fromEntity, toEntity,
                relationship, null));
    }

}
