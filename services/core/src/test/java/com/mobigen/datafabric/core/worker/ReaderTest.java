package com.mobigen.datafabric.core.worker;

import com.mobigen.datafabric.core.worker.queue.Queue;
import com.mobigen.datafabric.core.worker.queue.QueueImpl;
import com.mobigen.datafabric.core.worker.queue.QueueMode;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

class ReaderTest {

    @Test
    void Q1T1_Start() {
        LinkedHashMap<String, Queue<Job>> queues = new LinkedHashMap<>();
        Queue<Job> queue;

        queue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );
        queues.put( "Queue:0", queue );

        Reader reader = new Reader( queues, null, null );

        Runnable runnable1 = () -> reader.start( 1 );
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
        LinkedHashMap<String, Queue<Job>> queues = new LinkedHashMap<>();
        Queue<Job> queue;

        queue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );
        queues.put( "Queue:0", queue );

        Reader reader = new Reader( queues, null, null );

        queue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );
        reader.addQueue( "Queue:1", queue );

        Runnable runnable1 = () -> reader.start( 1 );
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
        LinkedHashMap<String, Queue<Job>> queues = new LinkedHashMap<>();
        Queue<Job> queue;

        queue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );
        queues.put( "Queue:0", queue );

        Reader reader = new Reader( queues, null , null);

        queue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );
        reader.addQueue( "Queue:1", queue );

        queue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );
        reader.addQueue( "Queue:2", queue );

        Runnable runnable1 = () -> reader.start( 2 );
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