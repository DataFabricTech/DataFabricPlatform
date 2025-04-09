package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.domain.MonitoringLog;
import com.mobigen.monitoring.repository.MonitoringTaskRepository;
import com.mobigen.monitoring.service.ModelService;
import com.mobigen.monitoring.service.openMetadata.OpenMetadataService;
import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {
    private final MonitoringTaskRepository monitoringTaskRepository;
    private final DatabaseManagementService databaseManagementServiceImpl;
    private final OpenMetadataService openMetadataService;
    private final ModelService modelService;

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
        return modelService.getModelCountFromOM(UUID.fromString("d197db55-85df-455d-8cf0-b89b8820780e"));
    }

    @GetMapping("/cpuUsed")
    public Object cpuUsed(@RequestParam(required = false) String serviceId) {
        if (serviceId == null || serviceId.isEmpty())
            return databaseManagementServiceImpl.getCpuSpentTime();
        else
            return databaseManagementServiceImpl.getCpuSpentTime(serviceId);
    }

    @GetMapping("/memoryUsed")
    public Object memoryUsed(@RequestParam(required = false) String serviceId) {
        if (serviceId == null || serviceId.isEmpty())
            return databaseManagementServiceImpl.getMemoryUsage();
        else
            return databaseManagementServiceImpl.getMemoryUsage(serviceId);
    }

    @GetMapping("/diskUsage")
    public Object diskUsage(@RequestParam(required = false) String serviceId) {
        if (serviceId == null || serviceId.isEmpty())
            return databaseManagementServiceImpl.getAllDiskUsage();
        else
            return databaseManagementServiceImpl.getDiskUsage(serviceId);
    }

    @GetMapping("/avgRequestRate")
    public Object getAvgRequestRate(@RequestParam(required = false) String serviceId) {
        if (serviceId == null || serviceId.isEmpty())
            return databaseManagementServiceImpl.getAverageQueryOutcome();
        else
            return databaseManagementServiceImpl.getAverageQueryOutcome(serviceId);
    }

    @GetMapping("/slow-query")
    public Object getSlowQuery(@RequestParam(required = false) String serviceId) {
        if (serviceId == null || serviceId.isEmpty())
            return databaseManagementServiceImpl.getSlowQueries();
        else
            return databaseManagementServiceImpl.getSlowQueries(serviceId);
    }

    @GetMapping("databaseServices")
    public Object getDatabaseServices() {
        return openMetadataService.getDatabaseServices();
    }
}
