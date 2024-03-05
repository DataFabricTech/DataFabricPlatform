package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.StorageAdaptorSchema;
import dto.enums.AdaptorType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public interface StorageAdaptorSchemaRepository extends JpaRepository<StorageAdaptorSchema,UUID> {
    List<StorageAdaptorSchema> findByName(String name, Pageable pageable);
    List<StorageAdaptorSchema> findByAdaptorType(AdaptorType adaptorType, Pageable pageable);
    List<StorageAdaptorSchema> findByEnable(boolean enable, Pageable pageable);
}
