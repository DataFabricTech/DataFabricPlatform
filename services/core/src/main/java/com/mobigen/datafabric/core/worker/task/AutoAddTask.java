package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.job.Job;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoAddTask implements Runnable{
    private final Job job;
    public AutoAddTask( Job job) {
        this.job = job;
    }

    @Override
    public void run() {
       log.error( "[ Auto Add Task ] Start : OK" );
       log.error( "[ Auto Add Task ] Job Info[ {} ]", job.toString() );
    }
}
