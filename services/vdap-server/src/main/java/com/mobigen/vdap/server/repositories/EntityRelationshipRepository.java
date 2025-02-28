package com.mobigen.vdap.server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobigen.vdap.server.entity.EntityRelationshipEntity;
import com.mobigen.vdap.server.entity.EntityRelationshipId;

@Repository
public interface EntityRelationshipRepository extends JpaRepository<EntityRelationshipEntity, EntityRelationshipId> {

    List<EntityRelationshipEntity> findByFromIdAndFromEntityAndRelationIn(String fromId, String fromEntity, List<Integer> relation);

    List<EntityRelationshipEntity> findByFromIdAndFromEntityAndToEntityAndRelation(String fromId, String fromEntity, String toEntity, Integer relation);
}