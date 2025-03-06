package com.mobigen.monitoring.vo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class OracleConnectionType {
    private String oracleServiceName;
    private String databaseSchema;
}
