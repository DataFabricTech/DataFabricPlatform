package com.mobigen.vdap.server.repositories;

import com.mobigen.vdap.server.entity.EntityRelationshipEntity;
import com.mobigen.vdap.server.entity.EntityRelationshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntityRelationshipRepository extends JpaRepository<EntityRelationshipEntity, EntityRelationshipId> {

    List<EntityRelationshipEntity> findByFromIdAndFromEntityAndRelationIn(String fromId, String fromEntity, List<Integer> relation);

    List<EntityRelationshipEntity> findByFromIdAndFromEntityAndToEntityAndRelation(String fromId, String fromEntity, String toEntity, Integer relation);

    List<EntityRelationshipEntity> findByToIdAndToEntityAndRelationAndFromEntity(String toId, String toEntity, Integer relation, String fromEntity);

    void deleteByToEntityAndToId(String toEntity, String toId);

    void deleteByFromEntityAndFromId(String fromEntity, String fromId);

    List<EntityRelationshipEntity> findByToIdAndToEntityAndRelation(String toId, String toEntity, Integer relation);
}