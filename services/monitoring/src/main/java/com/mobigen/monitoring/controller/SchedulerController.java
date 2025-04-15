package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.service.scheduler.DynamicSchedulerService;
import com.mobigen.monitoring.service.storage.DatabaseManagementService;
import org.jose4j.jwk.Use;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final DynamicSchedulerService schedulerService;
    private final DatabaseManagementService databaseManagementService;

    public SchedulerController(DynamicSchedulerService schedulerService, final DatabaseManagementService databaseManagementService) {
        this.schedulerService = schedulerService;
        this.databaseManagementService = databaseManagementService;
    }

    @PostMapping("/add")
    public String addTask(@RequestParam String serviceId, @RequestParam Integer period) {
        databaseManagementService.enableMonitoring(serviceId, period);
        schedulerService.addSchedule(serviceId, period);
        return "Task 추가됨: " + serviceId;
    }

    @PostMapping("/remove")
    public String removeTask(@RequestParam String serviceId) {
        databaseManagementService.disableMonitoring(serviceId);

        schedulerService.cancel(serviceId);

        return "Task 제거됨: " + serviceId;
    }

    @GetMapping("/list")
    public Object listTasks() {
        return schedulerService.getTasks();
    }
}
