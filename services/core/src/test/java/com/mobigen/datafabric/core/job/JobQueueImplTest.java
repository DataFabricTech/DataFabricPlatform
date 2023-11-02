package com.mobigen.datafabric.core.job;

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
            new JobQueueImpl(QueueMode.BLOCKING, 100 );
        } );
    }

    @Test
    void initializeBlockingNoLimit() {
        assertDoesNotThrow( () -> {
            new JobQueueImpl(QueueMode.BLOCKING, null );
        } );
    }

    @Test
    void initializeNonBlocking() {
        assertDoesNotThrow( () -> {
            new JobQueueImpl(QueueMode.NON_BLOCKING, 100 );
        } );
    }

    @Test
    void initializeNonBlockingNoLimit() {
        assertDoesNotThrow( () -> {
            new JobQueueImpl(QueueMode.NON_BLOCKING, null );
        } );
    }

    @Test
    void pushBlocking() throws Exception {
        JobQueue queue = new JobQueueImpl( QueueMode.BLOCKING, 10 );
        for( int i = 0; i < 10; i++ ) {
            assertDoesNotThrow( () -> {
                assertEquals( QueueResult.SUCCESS, queue.push( Job.builder().type( JobType.STORAGE_ADD ).storage( "storage-id" ).build() ) );
            } );
        }

        // pop thread
        Runnable runnable = () -> {
            try {
                Thread.sleep( 200 );
                queue.pop();
                System.out.println( "pop" );
            } catch( Exception e ) {
                throw new RuntimeException( e );
            }
        };
        Thread t=new Thread(runnable);
        t.start();
        assertTimeout( Duration.ofMillis( 100 ), () -> {
            try {
                System.out.println( "push" );
                queue.push( Job.builder().type( JobType.STORAGE_ADD ).storage( "storage-id" ).build() );
            } catch( Exception e ) {
                throw new RuntimeException( e );
            }
        } );
    }

    @Test
    void pushNonBlocking() throws Exception {
        JobQueue finalQueue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );
        for( int i = 0; i < 10; i++ ) {
            assertDoesNotThrow( () -> {
                assertEquals( QueueResult.SUCCESS, finalQueue.push( Job.builder().type( JobType.STORAGE_ADD ).storage( "storage-id" ).build() ) );
            } );
        }
        assertEquals( QueueResult.FAIL, finalQueue.push( Job.builder().type( JobType.STORAGE_ADD ).storage( "storage-id" ).build() ) );
    }

    @Test
    void popBlocking() throws Exception {
        JobQueue finalQueue = new JobQueueImpl(  QueueMode.NON_BLOCKING, 10 );
        Runnable runnable = () -> {
            try {
                Thread.sleep( 200 );
            } catch( InterruptedException e ) {
                throw new RuntimeException( e );
            }
            for( int i = 0; i < 10; i++ ) {
                assertDoesNotThrow( () -> {
                    assertEquals( QueueResult.SUCCESS, finalQueue.push( Job.builder().type( JobType.STORAGE_ADD ).storage( "storage-id" ).build() ) );
                } );
            }
        };
        Thread t=new Thread(runnable);
        t.start();

        System.out.println( "Start Pop" );
        for( int i = 0; i < 10; i++ ) {
            assertNotNull( finalQueue.pop(), "pop success" );
        }
    }

    @Test
    void popNonBlocking() throws Exception {
        JobQueue finalQueue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );
        assertNull( finalQueue.pop() );

        for( int i = 0; i < 10; i++ ) {
            assertDoesNotThrow( () -> {
                assertEquals( QueueResult.SUCCESS, finalQueue.push( Job.builder().type( JobType.STORAGE_ADD ).storage( "storage-id" ).build() ) );
            } );
        }
        for( int i = 0; i < 10; i++ ) {
            assertNotNull( finalQueue.pop(), "pop success" );
        }
    }

    @Test
    void pushAndPopInThread() throws Exception {
        JobQueue finalQueue = new JobQueueImpl( QueueMode.NON_BLOCKING, 10 );

        // Create Fixed Thread Pool
        ExecutorService executor = Executors.newFixedThreadPool( 10 );
        IntStream.range( 0, 10 ).forEach( n -> executor.execute( () -> {
            try {
                if( n % 2 == 0 ) {
                    finalQueue.push( Job.builder().type( JobType.STORAGE_ADD ).storage( "storage-id" ).build() );
                } else {
                    finalQueue.pop();
                }
            } catch( Exception e ) {
                throw new RuntimeException( e );
            }
        } ) );
    }
}