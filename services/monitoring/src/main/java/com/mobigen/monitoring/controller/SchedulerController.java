package com.mobigen.monitoring.controller;

import com.mobigen.monitoring.service.scheduler.DynamicSchedulerService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final DynamicSchedulerService schedulerService;

    public SchedulerController(DynamicSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/add")
    public String addTask(@RequestParam String serviceId, @RequestParam Integer period) {
        schedulerService.addSchedule(serviceId, period);
        return "Task 추가됨: " + serviceId;
    }

    @PostMapping("/remove")
    public String removeTask(@RequestParam String serviceId) {
        schedulerService.cancel(serviceId);
        return "Task 제거됨: " + serviceId;
    }
}
