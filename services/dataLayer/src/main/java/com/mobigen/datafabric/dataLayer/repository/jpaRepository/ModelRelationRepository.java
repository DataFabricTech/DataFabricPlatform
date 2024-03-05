package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelRelation;
import dto.compositeKeys.ModelRelationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModelRelationRepository extends JpaRepository<ModelRelation, ModelRelationKey> {
    List<ModelRelation> findByChildModelId(UUID childId);
}
