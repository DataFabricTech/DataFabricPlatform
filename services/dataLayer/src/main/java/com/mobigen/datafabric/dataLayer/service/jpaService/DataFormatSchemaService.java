package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.DataFormatSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DataFormatSchemaService extends SyncService<DataFormatSchema, UUID> {
    public DataFormatSchemaService(JpaRepository<DataFormatSchema, UUID> repository) {
        super(repository);
    }
}
