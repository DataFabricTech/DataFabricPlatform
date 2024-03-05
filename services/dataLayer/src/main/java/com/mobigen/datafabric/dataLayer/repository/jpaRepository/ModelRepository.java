package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.Model;
import dto.enums.FormatType;
import dto.enums.StatusType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ModelRepository extends JpaRepository<Model, UUID> {
    List<Model> findByName(String name, Pageable pageable);
    List<Model> findByFormatType(FormatType formatType, Pageable pageable);
    List<Model> findByStorageId(UUID storageId, Pageable pageable);
    List<Model> findByStatus(StatusType statusType, Pageable pageable);
    List<Model> findByCreatedBy(UUID createdBy, Pageable pageable);
    List<Model> findByModifiedBy(UUID modifiedBy, Pageable pageable);
    List<Model> findBySyncEnable(boolean syncEnable, Pageable pageable);
    List<Model> findByCreatedAtBefore(LocalDateTime endTime, Pageable pageable);
    List<Model> findByCreatedAtAfter(LocalDateTime startTime, Pageable pageable);
    List<Model> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    List<Model> findByModifiedAtBefore(LocalDateTime endTime, Pageable pageable);
    List<Model> findByModifiedAtAfter(LocalDateTime startTime, Pageable pageable);
    List<Model> findByModifiedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
