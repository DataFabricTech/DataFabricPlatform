package com.mobigen.datafabric.core.worker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Worker {
    private final static int CORE_POOL_SIZE = 1;
    ThreadPoolExecutor executor;

    public Worker( int maxThreadCount ) {
        log.error( "[ Worker-Pool ] Core Size[ {} ] Max Size[ {} ]", CORE_POOL_SIZE, maxThreadCount );
        this.executor = new ThreadPoolExecutor( CORE_POOL_SIZE, maxThreadCount, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() );
        log.error( "[ Worker-Pool ] Init : OK" );
    }

    public void runTask( Runnable task ) {
        executor.execute( task );
    }

    public void shutdown() {
        executor.shutdown();
        log.error( "[ Worker ] Thread Pool Shutdown : OK" );
    }

    public void monitoring() {
        log.error( "[ Worker ] Active[ {} ] PoolSize[ {} ], TaskQueue[ {} ]", this.executor.getActiveCount(), this.executor.getPoolSize(), this.executor.getQueue().size() );
    }
}
