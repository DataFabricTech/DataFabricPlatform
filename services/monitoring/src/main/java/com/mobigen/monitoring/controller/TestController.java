package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.domain.MonitoringLog;
import com.mobigen.monitoring.domain.Services;
import com.mobigen.monitoring.repository.MonitoringTaskRepository;
import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {
    private final MonitoringTaskRepository monitoringTaskRepository;
    private final DatabaseManagementService databaseManagementServiceImpl;

    @GetMapping
    public Object getAll() {
        return monitoringTaskRepository.findAll();
    }

    @GetMapping("/save")
    public Object save() {
        return monitoringTaskRepository.save(
                MonitoringLog.builder()
                        .description("test")
                        .serviceId("test")
                        .taskType("save")
                        .status("RUNNING")
                        .build());
    }
//
//    @GetMapping("/getDatabases")
//    public Object getDatabases() {
//        return databaseManagementService.getTest();
//    }

    @GetMapping("table-model")
    public Object getTableModels() {
        return databaseManagementServiceImpl.getModelCountFromOM(UUID.fromString("d197db55-85df-455d-8cf0-b89b8820780e"));
    }
}
