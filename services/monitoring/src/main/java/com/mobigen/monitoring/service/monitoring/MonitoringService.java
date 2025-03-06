package com.mobigen.monitoring.service.monitoring;

import com.mobigen.monitoring.service.scheduler.DatabaseConnectionInfo;
import com.mobigen.monitoring.service.scheduler.MonitoringTask;
import com.mobigen.monitoring.service.scheduler.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MonitoringService {
    private final Timer timer;
    private final MonitoringTask timerTask;
    private final ConcurrentHashMap<String, DatabaseConnectionInfo> tasks;

    public MonitoringService() {
        this.tasks = new ConcurrentHashMap<>();
        this.timer = new Timer();
        this.timerTask = new MonitoringTask(this);
    }

    public Object addTask(TaskInfo taskInfo) {
        tasks.put(taskInfo.id(), taskInfo.databaseConnectionInfo());

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

    public MonitoringTask saveMonitoringTask(String serviceId) {
        // serviceId로 service name 조회
        // 연결 상태
        // 데이터 수
        // 데이터 변경 감지 (table row, table update time, file update time, file size)
        // 평균 응답 시간
        // history

        return null;
    }
}
