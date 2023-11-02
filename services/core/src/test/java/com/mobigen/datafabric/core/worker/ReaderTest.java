package com.mobigen.datafabric.core.worker;

import com.mobigen.datafabric.core.job.JobQueue;
import com.mobigen.datafabric.core.job.JobQueueImpl;
import com.mobigen.datafabric.core.job.QueueMode;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

class ReaderTest {

    @Test
    void Q1T1_Start() {
        LinkedHashMap<String, JobQueue> queues = new LinkedHashMap<>();
        JobQueue queue;

        queue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );
        queues.put( "queue:0", queue );

        Reader reader = new Reader( queues, null );

        Runnable runnable1 = () -> {
            reader.start( 1 );
        };
        Thread t = new Thread( runnable1 );
        t.start();
        try {
            Thread.sleep( 500 );
        } catch( InterruptedException e ) {
            throw new RuntimeException( e );
        }
        reader.stop();
    }
    @Test
    void Q2T1_Start() {
        LinkedHashMap<String, JobQueue> queues = new LinkedHashMap<>();
        JobQueue queue;

        queue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );
        queues.put( "queue:0", queue );

        Reader reader = new Reader( queues, null );

        queue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );
        reader.addQueue( "queue:1", queue );

        Runnable runnable1 = () -> {
            reader.start( 1 );
        };
        Thread t = new Thread( runnable1 );
        t.start();
        try {
            Thread.sleep( 500 );
        } catch( InterruptedException e ) {
            throw new RuntimeException( e );
        }
        reader.stop();
    }


    @Test
    void Q3T2_Start() {
        LinkedHashMap<String, JobQueue> queues = new LinkedHashMap<>();
        JobQueue queue;

        queue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );
        queues.put( "queue:0", queue );

        Reader reader = new Reader( queues, null );

        queue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );
        reader.addQueue( "queue:1", queue );

        queue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );
        reader.addQueue( "queue:2", queue );

        Runnable runnable1 = () -> {
            reader.start( 2 );
        };
        Thread t = new Thread( runnable1 );
        t.start();
        try {
            Thread.sleep( 500 );
        } catch( InterruptedException e ) {
            throw new RuntimeException( e );
        }
        reader.stop();
    }
}