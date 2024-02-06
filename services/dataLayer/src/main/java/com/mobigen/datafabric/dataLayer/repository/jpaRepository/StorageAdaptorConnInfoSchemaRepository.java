package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.StorageAdaptorConnInfoSchema;
import dto.compositeKeys.StorageAdaptorConnInfoSchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageAdaptorConnInfoSchemaRepository extends JpaRepository<StorageAdaptorConnInfoSchema, StorageAdaptorConnInfoSchemaKey> {
}
