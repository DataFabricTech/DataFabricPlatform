package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ModelMetadataSchema;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModelMetadataSchemaRepository extends JpaRepository<ModelMetadataSchema, UUID> {
}
