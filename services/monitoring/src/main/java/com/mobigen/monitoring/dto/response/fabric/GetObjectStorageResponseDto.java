package com.mobigen.monitoring.dto.response.fabric;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobigen.monitoring.service.scheduler.DatabaseConnectionInfo;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class GetObjectStorageResponseDto {
    private String id;
    private String name;
    private String fullyQualifiedName;
    private Long updatedAt;
    private String updatedBy;
    private Boolean deleted;
    private String serviceType;
    private String description;
    @JsonProperty("connection.config")
    private ObjectStorageConnectionInfo connection;
}
