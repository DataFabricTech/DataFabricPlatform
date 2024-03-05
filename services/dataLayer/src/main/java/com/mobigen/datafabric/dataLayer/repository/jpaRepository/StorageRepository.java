package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.Storage;
import dto.enums.StatusType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StorageRepository extends JpaRepository<Storage, UUID> {
    List<Storage> findByName(String name, Pageable pageable);
    List<Storage> findByCreatedBy(UUID createdBy, Pageable pageable);
    List<Storage> findByModifiedBy(UUID modifiedBy, Pageable pageable);
    List<Storage> findByStatus(StatusType statusType, Pageable pageable);
}
