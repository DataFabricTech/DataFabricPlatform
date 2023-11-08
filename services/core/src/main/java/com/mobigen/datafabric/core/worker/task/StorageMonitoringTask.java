package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.worker.Job;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StorageMonitoringTask implements Runnable {
    private final Job job;

    public StorageMonitoringTask( Job job ) {
        this.job = job;
    }

    @Override
    public void run() {
        log.error( "[ Storage Monitoring Task ] Start : OK" );
        log.error( "[ Storage Monitoring Task ] Job Info[ {} ]", job.toString() );
    }

}
