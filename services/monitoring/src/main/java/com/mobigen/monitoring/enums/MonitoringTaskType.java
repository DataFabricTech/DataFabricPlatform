package com.mobigen.monitoring.enums;

import lombok.Getter;

@Getter
public enum MonitoringTaskType {
    CONNECT_CHECK("connect-check"),
    COUNT_TABLE_DATA("count-table-data"),
    COUNT_OBJECT_STORAGE_DATA("count-object-storage-data"),
    DETECT_CHANGE_DATA("detect-change-data"),
    COLLECT_AVG_RESPONSE_TIME("collect-avg-response-time")
    ;

    private final String type;

    MonitoringTaskType(String type) {
        this.type = type;
    }
}
