package com.mobigen.datafabric.relationship.repository.fabric;

import com.mobigen.datafabric.relationship.dto.fabric.StorageContainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageContainerEntityRepository extends JpaRepository<StorageContainerEntity, String> {
}