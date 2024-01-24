package com.mobigen.datafabric.dataLayer.repository;

import dto.TableDataQuality;

public class TableDataQualityRepository extends JPARepository<TableDataQuality> {
    public TableDataQualityRepository() {
        super(TableDataQuality.class);
    }
}
