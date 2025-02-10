package com.mobigen.monitoring.vo;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DatabaseConnectionInfo {
    private String url;
    private String username;
    private String password;
    private String database;
}
