package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.ColumnMetadata;
import dto.compositeKeys.ColumnMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ColumnMetadataService extends JpaService<ColumnMetadata, ColumnMetadataKey> {
    public ColumnMetadataService(JpaRepository<ColumnMetadata, ColumnMetadataKey> repository) {
        super(repository);
    }
}
