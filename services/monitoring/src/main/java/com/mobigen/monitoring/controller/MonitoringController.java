package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.annotation.CommonResponse;
import com.mobigen.monitoring.dto.request.TaskId;
import com.mobigen.monitoring.enums.DatabaseType;
import com.mobigen.monitoring.service.monitoring.MonitoringService;
import com.mobigen.monitoring.service.timer.ConnectionInfo;
import com.mobigen.monitoring.service.timer.TaskInfo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/monitoring/v1")
public class MonitoringController {
    private final MonitoringService monitoringService;

    public MonitoringController() {
        this.monitoringService = new MonitoringService(null);
    }

    @GetMapping("/start")
    @CommonResponse
    public Object start() {
        return monitoringService.start();
    }

    @PostMapping("/edit")
    @CommonResponse
    public Object editTask(@RequestBody TaskInfo body) {
        return monitoringService.addTask(body);
    }

    @PostMapping("/remove")
    @CommonResponse
    public Object removeTask(@RequestBody TaskId body) {
        return monitoringService.removeTask(body.getId());
    }

    @GetMapping("/service-type")
    @CommonResponse
    public Object serviceType() {
        return DatabaseType.values();
    }
}
