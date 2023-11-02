package com.mobigen.datafabric.core.job;

public enum JobType {
    // 저장소(연결정보) 이벤트 작업
    STORAGE_ADD,
    STORAGE_UPDATE,
    STORAGE_DELETE,
    // 저장소 관련 세부 동작
    AUTO_DATA_CATALOG_GENERATE,
    STORAGE_SYNC,
    STORAGE_MONITORING;
    // And...
}
