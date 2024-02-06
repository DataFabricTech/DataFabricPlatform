package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.StorageConnInfo;
import dto.compositeKeys.StorageConnInfoKey;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageConnInfoRepository extends JpaRepository<StorageConnInfo, StorageConnInfoKey> {
}
