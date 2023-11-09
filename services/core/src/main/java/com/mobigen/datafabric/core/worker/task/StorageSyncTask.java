package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.worker.Job;
import com.mobigen.datafabric.core.worker.timer.TimerCallback;
import com.mobigen.datafabric.core.worker.timer.TimerData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StorageSyncTask implements Runnable, TimerCallback {
    private final Job job;
    public StorageSyncTask( Job job) {
        this.job = job;
    }
    @Override
    public void run() {
        log.error( "[ Storage Sync Task ] Start : OK" );
        log.error( "[ Storage Sync Task ] Job Info[ {} ]", job.toString() );
    }

    @Override
    public void callback( TimerData data ) {

    }
}
