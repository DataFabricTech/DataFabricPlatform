package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.StorageAdaptorSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StorageAdaptorSchemaRepository extends JpaRepository<StorageAdaptorSchema,UUID> {
}
