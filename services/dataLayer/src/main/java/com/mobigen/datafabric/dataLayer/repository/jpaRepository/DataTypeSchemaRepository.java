package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.DataTypeSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DataTypeSchemaRepository extends JpaRepository<DataTypeSchema, UUID> {
}
