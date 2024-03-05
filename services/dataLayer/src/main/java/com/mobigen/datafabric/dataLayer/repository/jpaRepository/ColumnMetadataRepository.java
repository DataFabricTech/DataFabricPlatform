package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ColumnMetadata;
import dto.compositeKeys.ColumnMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnMetadataRepository extends JpaRepository<ColumnMetadata, ColumnMetadataKey> {
    List<ColumnMetadata> findByName(String name);
}
