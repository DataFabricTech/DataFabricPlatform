package com.mobigen.vdap.server.relationship;

import com.mobigen.vdap.server.entity.RelationshipEntity;
import com.mobigen.vdap.server.entity.RelationshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipRepository extends JpaRepository<RelationshipEntity, RelationshipId>, JpaSpecificationExecutor<RelationshipEntity> {
    List<RelationshipEntity> findByToIdAndToEntityAndRelation(String toId, String toEntity, Integer relation);
}