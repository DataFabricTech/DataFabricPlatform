package com.mobigen.monitoring.enums;

import lombok.Getter;

@Getter
public enum MonitoringTaskStatus {
    RUNNING("running"),
    ERROR("error"),
    COMPLETED("completed"),;

    private final String value;

    MonitoringTaskStatus(String value) {
        this.value = value;
    }
}
