package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.DataTypeOptionSchema;
import dto.compositeKeys.DataTypeOptionSchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataTypeOptionSchemaRepository extends JpaRepository<DataTypeOptionSchema, DataTypeOptionSchemaKey> {}
