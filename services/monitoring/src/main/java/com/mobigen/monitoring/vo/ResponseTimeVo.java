package com.mobigen.monitoring.vo;

import java.util.UUID;

public interface ResponseTimeVo {
    UUID getServiceId();
    String getServiceName();
    String getServiceDisplayName();
    Long getExecuteAt();
    Long getQueryExecutionTime();
}
