package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.job.Job;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StorageSyncTask implements Runnable {
    private final Job job;
    public StorageSyncTask( Job job) {
        this.job = job;
    }
    @Override
    public void run() {
        log.error( "[ Storage Sync Task ] Start : OK" );
        log.error( "[ Storage Sync Task ] Job Info[ {} ]", job.toString() );
    }
}
