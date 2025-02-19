package com.mobigen.monitoring.vo;

import com.mobigen.monitoring.enums.ConnectionStatus;

import java.util.UUID;

public interface ConnectionHistoryVo {
    public UUID getServiceId();
    public String getServiceName();
    public String getServiceDisplayName();
    public String getServiceType();
    public ConnectionStatus getConnectionStatus();
}
