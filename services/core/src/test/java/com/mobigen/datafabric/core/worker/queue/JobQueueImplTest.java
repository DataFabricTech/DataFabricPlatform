package com.mobigen.datafabric.core.worker.queue;

import com.mobigen.datafabric.core.worker.Job;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueImplTest {

    @Test
    void initializeBlocking() {
        assertDoesNotThrow( () -> {
            new QueueImpl<Job>(QueueMode.BLOCKING, 100 );
        } );
    }

    @Test
    void initializeBlockingNoLimit() {
        assertDoesNotThrow( () -> {
            new QueueImpl<Job>(QueueMode.BLOCKING, null );
        } );
    }

    @Test
    void initializeNonBlocking() {
        assertDoesNotThrow( () -> {
            new QueueImpl<Job>(QueueMode.NON_BLOCKING, 100 );
        } );
    }

    @Test
    void initializeNonBlockingNoLimit() {
        assertDoesNotThrow( () -> {
            new QueueImpl<Job>(QueueMode.NON_BLOCKING, null );
        } );
    }

    @Test
    void pushBlocking() throws Exception {
        Queue<Job> queue = new QueueImpl<>( QueueMode.BLOCKING, 10 );
        for( int i = 0; i < 10; i++ ) {
            assertDoesNotThrow( () -> assertTrue( queue.add( Job.builder().type( Job.JobType.STORAGE_ADD ).storageId( "storage-id" ).build() ) ));
        }

        // pop thread
        Runnable runnable = () -> {
            try {
                Thread.sleep( 200 );
                queue.poll();
                System.out.println( "pop" );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        Thread t=new Thread(runnable);
        t.start();
        long start = System.currentTimeMillis();
        queue.add( Job.builder().type( Job.JobType.STORAGE_ADD ).storageId( "storage-id" ).build() );
        long end = System.currentTimeMillis();
        assertTrue( Duration.ofMillis( end - start ).compareTo( Duration.ofMillis( 200 ) ) > 0 );
    }

    @Test
    void pushNonBlocking() throws Exception {
        Queue<Job> finalQueue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );
        for( int i = 0; i < 10; i++ ) {
            assertDoesNotThrow( () -> assertTrue( finalQueue.add( Job.builder().type( Job.JobType.STORAGE_ADD ).storageId( "storage-id" ).build() ) ));
        }
        assertFalse( finalQueue.add( Job.builder().type( Job.JobType.STORAGE_ADD ).storageId( "storage-id" ).build() ) );
    }

    @Test
    void popBlocking() throws Exception {
        Queue<Job> finalQueue = new QueueImpl<>(  QueueMode.BLOCKING, 10 );
        Runnable runnable = () -> {
            try {
                Thread.sleep( 200 );
            } catch( InterruptedException e ) {
                throw new RuntimeException( e );
            }
            for( int i = 0; i < 10; i++ ) {
                assertDoesNotThrow( () -> assertTrue( finalQueue.add( Job.builder().type( Job.JobType.STORAGE_ADD ).storageId( "storage-id" ).build() ) ));
            }
        };
        Thread t=new Thread(runnable);
        t.start();

        System.out.println( "Start Pop" );
        for( int i = 0; i < 10; i++ ) {
            assertNotNull( finalQueue.poll(), "pop success" );
        }
    }

    @Test
    void popNonBlocking() throws Exception {
        Queue<Job> finalQueue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );
        assertNull( finalQueue.poll() );

        for( int i = 0; i < 10; i++ ) {
            assertDoesNotThrow( () -> assertTrue( finalQueue.add( Job.builder().type( Job.JobType.STORAGE_ADD ).storageId( "storage-id" ).build() ) ));
        }
        for( int i = 0; i < 10; i++ ) {
            assertNotNull( finalQueue.poll(), "pop success" );
        }
    }

    @Test
    void pushAndPopInThread() {
        Queue<Job> finalQueue = new QueueImpl<>( QueueMode.NON_BLOCKING, 10 );

        // Create Fixed Thread Pool
        ExecutorService executor = Executors.newFixedThreadPool( 10 );
        IntStream.range( 0, 10 ).forEach( n -> executor.execute( () -> {
            try {
                if( n % 2 == 0 ) {
                    finalQueue.add( Job.builder().type( Job.JobType.STORAGE_ADD ).storageId( "storage-id" ).build() );
                } else {
                    finalQueue.poll();
                }
            } catch( Exception e ) {
                throw new RuntimeException( e );
            }
        } ) );
    }
}