package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelTag;
import dto.compositeKeys.ModelTagKey;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelTagRepository extends JpaRepository<ModelTag, ModelTagKey> {
}
