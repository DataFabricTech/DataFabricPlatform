package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StorageRepository extends JpaRepository<Storage, UUID> {
}
