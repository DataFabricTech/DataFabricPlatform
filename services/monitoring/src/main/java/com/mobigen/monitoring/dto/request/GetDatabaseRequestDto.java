package com.mobigen.monitoring.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetDatabaseRequestDto {
    private String query;
    private DatabaseConnectionRequest databaseConnection;
}
