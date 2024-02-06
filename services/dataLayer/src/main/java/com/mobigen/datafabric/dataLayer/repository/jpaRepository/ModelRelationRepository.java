package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelRelation;
import dto.compositeKeys.ModelRelationKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRelationRepository extends JpaRepository<ModelRelation, ModelRelationKey> {
}
