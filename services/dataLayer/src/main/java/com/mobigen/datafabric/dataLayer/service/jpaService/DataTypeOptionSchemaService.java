package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.DataTypeOptionSchema;
import dto.compositeKeys.DataTypeOptionSchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DataTypeOptionSchemaService extends JpaService<DataTypeOptionSchema, DataTypeOptionSchemaKey> {
    public DataTypeOptionSchemaService(JpaRepository<DataTypeOptionSchema, DataTypeOptionSchemaKey> repository) {
        super(repository);
    }
}
