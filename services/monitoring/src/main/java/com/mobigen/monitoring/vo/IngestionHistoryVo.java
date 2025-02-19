package com.mobigen.monitoring.vo;

import java.util.UUID;

public interface IngestionHistoryVo {
    public Long getEventAt();
    public String getIngestionName();
    public String getType();
    public String getEvent();
    public String getState();
    public UUID getServiceId();
    public String getServiceName();
    public String getServiceDisplayName();
    public String getDbType();

}