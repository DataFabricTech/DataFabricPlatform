package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.annotation.CommonResponse;
import com.mobigen.monitoring.dto.request.DatabaseConnectionRequest;
import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import com.mobigen.monitoring.service.storage.DatabaseManagementServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * database 에서 table 관련 함수 테스트하기 위해 만든 API
 * */
@RestController
@RequestMapping("/api/v1/database")
public class DatabaseConnectionController {

    private final DatabaseManagementService databaseConnectionService;

    public DatabaseConnectionController(DatabaseManagementServiceImpl databaseConnectionService) {
        this.databaseConnectionService = databaseConnectionService;
    }

    @PostMapping("/test-connection")
    public ResponseEntity<String> testDatabaseConnection(@RequestBody DatabaseConnectionRequest request) {
        Boolean isConnected = databaseConnectionService.checkServiceConnection(request);

        if (isConnected == null) {
            return ResponseEntity.ok("연결 체크 패스: (db type: " + request.getDbType() + ")");
        } else {
            if (isConnected) {
                return ResponseEntity.ok("✅ 연결 성공: " + request.getDbType() + " (" + request.getHost() + ")");
            } else {
                return ResponseEntity.status(500).body("❌ 연결 실패: " + request.getDbType() + " (" + request.getHost() + ")");
            }
        }
    }

    @PostMapping("/test-database")
    @CommonResponse
    public Object getDatabases(@RequestBody List<DatabaseConnectionRequest> requests) {
        Map<String, List<String>> result = new HashMap<>();

        for (DatabaseConnectionRequest request : requests) {
            result.put(request.getDbType(), databaseConnectionService.getDatabases(request));
        }

        return result;
    }

    @PostMapping("/test-table")
    @CommonResponse
    public Object getTables(@RequestBody DatabaseConnectionRequest request) {
        return databaseConnectionService.getTables(request);
    }

    @GetMapping("/test-row")
    @CommonResponse
    public Object getRows(@RequestParam String serviceId) {
        return databaseConnectionService.getTableRows(serviceId);
    }

    @PostMapping("/test-schema")
    @CommonResponse
    public Object getSchema(@RequestBody DatabaseConnectionRequest request) {
//        return databaseConnectionService.getTableSchema(request);
        return null;
    }
}
