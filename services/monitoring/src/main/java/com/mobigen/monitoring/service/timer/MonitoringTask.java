package com.mobigen.monitoring.service.timer;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class MonitoringTask extends TimerTask {
    private ConcurrentHashMap<String, ConnectionInfo> tasks;

    public MonitoringTask(ConcurrentHashMap<String, ConnectionInfo> tasks) {
        this.tasks = tasks;
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
