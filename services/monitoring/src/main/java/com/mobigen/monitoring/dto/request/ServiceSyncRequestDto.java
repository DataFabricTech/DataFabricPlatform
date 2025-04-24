package com.mobigen.monitoring.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceSyncRequestDto {
    private String serviceId;
    private String event; // CREATED, UPDATED , DELETED
    private Boolean isHardDelete;
    private String serviceModelType; // DATABASE_SERVICE, STORAGE_SERVICE
}
