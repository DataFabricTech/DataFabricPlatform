package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.DataFormatOptionSchema;
import dto.compositeKeys.DataFormatOptionSchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DataFormatOptionSchemaService extends SyncService<DataFormatOptionSchema, DataFormatOptionSchemaKey> {
    public DataFormatOptionSchemaService(JpaRepository<DataFormatOptionSchema, DataFormatOptionSchemaKey> repository) {
        super(repository);
    }
}
