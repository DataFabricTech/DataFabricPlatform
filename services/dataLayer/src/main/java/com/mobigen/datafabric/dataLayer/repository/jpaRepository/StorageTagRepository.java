package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.StorageTag;
import dto.compositeKeys.StorageTagKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageTagRepository extends JpaRepository<StorageTag, StorageTagKey> {
}
