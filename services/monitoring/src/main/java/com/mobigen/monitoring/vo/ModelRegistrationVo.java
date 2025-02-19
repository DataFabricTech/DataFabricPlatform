package com.mobigen.monitoring.vo;

import java.util.UUID;

public interface ModelRegistrationVo {
    public UUID getServiceId();
    public String getServiceName();
    public String getServiceDisplayName();
    public Integer getOpenMetadataModelCount();
    public Integer getModelCount();
}
