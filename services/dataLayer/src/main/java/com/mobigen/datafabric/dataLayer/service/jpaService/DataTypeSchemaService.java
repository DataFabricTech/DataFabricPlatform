package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.DataTypeSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DataTypeSchemaService extends JpaService<DataTypeSchema, UUID> {
    public DataTypeSchemaService(JpaRepository<DataTypeSchema, UUID> repository) {
        super(repository);
    }
}
