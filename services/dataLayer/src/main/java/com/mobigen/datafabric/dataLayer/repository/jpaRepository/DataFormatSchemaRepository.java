package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.DataFormatSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DataFormatSchemaRepository extends JpaRepository<DataFormatSchema, UUID> {
}
