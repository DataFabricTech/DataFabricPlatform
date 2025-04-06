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
    public String addTask(@RequestParam String id, @RequestParam String cron) {
        schedulerService.addTask(id, () -> {
            System.out.println("Task [" + id + "] 실행됨: " + LocalDateTime.now());
        }, cron);
        return "Task 추가됨: " + id;
    }

    @PostMapping("/remove")
    public String removeTask(@RequestParam String id) {
        schedulerService.removeTask(id);
        return "Task 제거됨: " + id;
    }

    @PostMapping("/update")
    public String updateTask(@RequestParam String id, @RequestParam String cron) {
        schedulerService.updateTask(id, cron);
        return "Task 업데이트됨: " + id;
    }

    @GetMapping("/list")
    public Set<String> listTasks() {
        return schedulerService.getRunningTaskIds();
    }
}
