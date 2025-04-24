package com.mobigen.monitoring.vo;

import com.mobigen.monitoring.enums.ConnectionStatus;

public interface ServicesResponse {
    public String getServiceId();
    public String getServiceName();
    public String getServiceDisplayName();
    public String getServiceType();
    public Long  getCreatedAt();
    public boolean getDeleted();
    public ConnectionStatus getConnectionStatus();
}
