package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.annotation.CommonResponse;
import com.mobigen.monitoring.dto.request.DatabaseConnectionRequest;
import com.mobigen.monitoring.dto.request.GetDatabaseRequestDto;
import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/database")
public class DatabaseConnectionController {

    private final DatabaseManagementService databaseConnectionService;

    public DatabaseConnectionController(DatabaseManagementService databaseConnectionService) {
        this.databaseConnectionService = databaseConnectionService;
    }

    @PostMapping("/test-connection")
    public ResponseEntity<String> testDatabaseConnection(@RequestBody DatabaseConnectionRequest request) {
        boolean isConnected = databaseConnectionService.testDatabaseConnection(request);

        if (isConnected) {
            return ResponseEntity.ok("✅ 연결 성공: " + request.getDbType() + " (" + request.getHost() + ")");
        } else {
            return ResponseEntity.status(500).body("❌ 연결 실패: " + request.getDbType() + " (" + request.getHost() + ")");
        }
    }

    @PostMapping("/test-database")
    @CommonResponse
    public Object getDatabases(@RequestBody List<GetDatabaseRequestDto> requests) {
        Map<String, List<String>> result = new HashMap<>();

        for (GetDatabaseRequestDto request : requests) {
            result.put(request.getDatabaseConnection().getDbType(), databaseConnectionService.getDatabases(request.getDatabaseConnection()));
        }

        return result;
    }

    @PostMapping("/test-table")
    @CommonResponse
    public Object getTables(@RequestBody GetDatabaseRequestDto request) {
        return databaseConnectionService.getTables(request.getDatabaseConnection());
    }

    @PostMapping("/test-row")
    @CommonResponse
    public Object getRows(@RequestBody GetDatabaseRequestDto request) {
        return databaseConnectionService.getRows(request.getDatabaseConnection());
    }

    @PostMapping("/test-schema")
    @CommonResponse
    public Object getSchema(@RequestBody GetDatabaseRequestDto request) {
        return databaseConnectionService.getSchema(request.getDatabaseConnection());
    }
}
