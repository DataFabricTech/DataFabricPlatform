package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.TableDataQuality;
import dto.compositeKeys.TableDataQualityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class TableDataQualityService extends SyncService<TableDataQuality, TableDataQualityKey> {
    public TableDataQualityService(JpaRepository<TableDataQuality, TableDataQualityKey> repository) {
        super(repository);
    }
}
