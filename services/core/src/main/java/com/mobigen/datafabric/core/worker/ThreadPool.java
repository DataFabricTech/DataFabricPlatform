package com.mobigen.datafabric.core.worker;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadPool {
    private final static int CORE_POOL_SIZE = 1;
    ThreadPoolExecutor executor;

    public ThreadPool( int maxThreadCount ) {
        log.error( "[ ThreadPool ] Core Size[ {} ] Max Size[ {} ]", CORE_POOL_SIZE, maxThreadCount );
        this.executor = new ThreadPoolExecutor( CORE_POOL_SIZE, maxThreadCount, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() );
        log.error( "[ ThreadPool ] Worker : OK" );
    }

    public void runTask( Runnable task ) {
        executor.execute( task );
    }

    public void shutdown() {
        executor.shutdown();
        log.error( "[ ThreadPool ] Thread Pool Shutdown : OK" );
    }

    public void monitoring() {
        log.error( "[ ThreadPool ] Active[ {} ] PoolSize[ {} ], TaskQueue[ {} ]",
                this.executor.getActiveCount(),
                this.executor.getPoolSize(),
                this.executor.getQueue().size() );
    }
}
