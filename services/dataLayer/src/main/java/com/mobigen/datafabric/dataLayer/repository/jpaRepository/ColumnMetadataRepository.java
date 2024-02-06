package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.ColumnMetadata;
import dto.compositeKeys.ColumnMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnMetadataRepository extends JpaRepository<ColumnMetadata, ColumnMetadataKey> {
}
