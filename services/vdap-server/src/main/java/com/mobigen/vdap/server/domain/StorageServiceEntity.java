package com.mobigen.vdap.server.domain;

import com.mobigen.vdap.schema.entity.services.ServiceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageServiceEntity {
    private String      id;
    private String      name;
    private ServiceType kind;
    private String      serviceType;
    private String      json;
    private Long        updatedAt;
    private String      updatedBy;
    private Boolean     deleted;
}
