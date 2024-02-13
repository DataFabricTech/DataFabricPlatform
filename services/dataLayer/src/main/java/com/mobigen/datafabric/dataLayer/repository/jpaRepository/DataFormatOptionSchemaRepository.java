package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.DataFormatOptionSchema;
import dto.compositeKeys.DataFormatOptionSchemaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataFormatOptionSchemaRepository extends JpaRepository<DataFormatOptionSchema, DataFormatOptionSchemaKey> {}
