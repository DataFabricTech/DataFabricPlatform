package com.mobigen.vdap.server.entity;

import com.mobigen.vdap.schema.entity.services.ServiceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StorageServiceEntity {
    private String        id;
    private String        name;
    private ServiceType   kind;
    private String        serviceType;
    private String        json;
    private LocalDateTime updatedAt;
    private String        updatedBy;
    private Boolean       deleted;
}
