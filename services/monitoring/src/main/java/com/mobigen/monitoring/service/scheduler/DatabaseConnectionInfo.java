package com.mobigen.monitoring.service.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobigen.monitoring.dto.response.fabric.AuthType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Slf4j
public class DatabaseConnectionInfo {
    private String type;
    private String scheme;
    private String databaseName;
    private String username;
    @JsonProperty("authType")
    private AuthType authType;
    private String password;
    private String hostPort;
}
