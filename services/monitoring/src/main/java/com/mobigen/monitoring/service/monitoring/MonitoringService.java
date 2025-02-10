package com.mobigen.monitoring.service.monitoring;

import com.mobigen.monitoring.service.timer.ConnectionInfo;
import com.mobigen.monitoring.service.timer.MonitoringTask;
import com.mobigen.monitoring.service.timer.TaskInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MonitoringService {
    private final Timer timer;
    private final MonitoringTask timerTask;
    private final ConcurrentHashMap<String, ConnectionInfo> tasks;

    public MonitoringService(final ConcurrentHashMap<String, ConnectionInfo> tasks) {
        this.tasks = tasks;
        this.timer = new Timer();
        this.timerTask = new MonitoringTask(tasks);
    }

    public Object addTask(TaskInfo taskInfo) {
        tasks.put(taskInfo.id(), taskInfo.connectionInfo());

        return "Successfully added task";
    }

    public Object removeTask(String taskId) {
        tasks.remove(taskId);
        return "Successfully removed task";
    }

    public Object start() {
        timer.schedule(timerTask, 0, 500);

        return "Successfully started";
    }
}
