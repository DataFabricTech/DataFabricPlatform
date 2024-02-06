package com.mobigen.datafabric.dataLayer.repository.jpaRepository;

import dto.TableDataQuality;
import dto.compositeKeys.TableDataQualityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableDataQualityRepository extends JpaRepository<TableDataQuality, TableDataQualityKey> {
}
