package com.mobigen.monitoring.dto.response.fabric;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobigen.monitoring.enums.ConnectionStatus;
import com.mobigen.monitoring.service.scheduler.DatabaseConnectionInfo;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class GetDatabasesResponseDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("fullyQualifiedName")
    private String fullyQualifiedName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("updatedAt")
    private Long updatedAt;

    @JsonProperty("updatedBy")
    private String updatedBy;

    @JsonProperty("serviceType")
    private String serviceType;

    @JsonProperty("connection")
    private DatabaseConnectionInfo connection;

    @JsonProperty("password")
    private String password;

    @JsonProperty("deleted")
    private Boolean deleted;

    private ConnectionStatus connectionStatus;

    private Long responseTime;
}
