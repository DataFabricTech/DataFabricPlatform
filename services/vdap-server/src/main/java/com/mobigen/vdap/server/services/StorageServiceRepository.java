package com.mobigen.vdap.server.services;

import com.mobigen.vdap.server.entity.StorageServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageServiceRepository extends JpaRepository<StorageServiceEntity, String>, JpaSpecificationExecutor<StorageServiceEntity> {

    Optional<StorageServiceEntity> findByName(String name);
}
