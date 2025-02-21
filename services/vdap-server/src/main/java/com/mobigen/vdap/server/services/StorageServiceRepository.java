package com.mobigen.vdap.server.services;

import com.mobigen.vdap.server.entity.StorageServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StorageServiceRepository extends JpaRepository<StorageServiceEntity, String> {
    @Query(value = "SELECT * FROM storage_service_entity WHERE id = ?1 AND deleted = ?2", nativeQuery = true)
    StorageServiceEntity findById(UUID id, Boolean deleted);
}
