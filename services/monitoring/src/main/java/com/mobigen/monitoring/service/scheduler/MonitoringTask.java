package com.mobigen.monitoring.service.scheduler;

import com.mobigen.monitoring.service.monitoring.MonitoringService;

import java.util.TimerTask;

public class MonitoringTask extends TimerTask {
    private final MonitoringService monitoringService;

    public MonitoringTask(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    public void sync() {
        // get db
    }

    @Override
    public void run() {
        // log
        // 1. sync
        // 2. schedule 정보로 연결해서 모니터링
        // 2.1 모니터링은 thread pool 에 던지기
        sync();
    }
}
