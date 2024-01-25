package com.mobigen.datafabric.dataLayer.repository;

import dto.TableDataQuality;
import jakarta.persistence.EntityManager;

public class TableDataQualityRepository extends JPARepository<TableDataQuality> {
    public TableDataQualityRepository(EntityManager em) {
        super(TableDataQuality.class, em);
    }
}
