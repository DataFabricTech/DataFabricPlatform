package com.mobigen.datafabric.dataLayer.service.jpaService;

import dto.TableDataQuality;
import dto.compositeKeys.TableDataQualityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class TableDataQualityService extends JpaService<TableDataQuality, TableDataQualityKey> {
    public TableDataQualityService(JpaRepository<TableDataQuality, TableDataQualityKey> repository) {
        super(repository);
    }
}
