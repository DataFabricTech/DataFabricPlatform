package com.mobigen.datafabric.core.worker;

import com.mobigen.datafabric.core.job.JobQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Reader {
    private Map<String, JobQueue> queues;
    private Worker worker;

    public Reader( Map<String, JobQueue> queues, Worker worker ) {
        if( queues != null ) {
            this.queues = new LinkedHashMap<>( queues );
            // LinkedHashMap Loop
            for( Map.Entry<String, JobQueue> entry : this.queues.entrySet() ) {
                log.error( "[ ReaderM ] Set Queue ID(Name) : {}", entry.getKey() );
            }
            log.info( "[ ReaderM ] Queue Size[ {} ]", this.queues.size() );
        } else {
            log.error( "[ ReaderM ] Queue NotSet" );
        }
        log.error( "[ ReaderM ] Init : OK" );
    }

    public void addQueue( String name, JobQueue queue ) {
        this.queues.put( name, queue );
        log.error( "[ ReaderM ] Add Queue ID(Name) : [ {} ]", name );
    }

    private List<ReaderThread> readers;
    private List<Thread> readerThreads;

    public void start( Integer threadCount ) {
        log.error( "[ ReaderM ] Sub Thread Size[ {} ]", threadCount );
        List<ReaderThread> readers = new ArrayList<>();
        List<Thread> readerThreads = new ArrayList<>();
        for( int i = 0; i < threadCount; i++ ) {
            readers.add( new ReaderThread( "Reader-" + i, this.worker ) );
        }

        // Set Queue To Sub Thread
        for( ReaderThread reader : readers ) {
            for( Map.Entry<String, JobQueue> entry : this.queues.entrySet() ) {
                reader.addQueue( entry.getKey(), entry.getValue() );
            }
        }

        // Start Sub Thread
        readers.forEach( readerThread -> {
            Thread thread = new Thread( readerThread );
            thread.start();
            readerThreads.add( thread );
        } );
        this.readers = readers;
        this.readerThreads = readerThreads;
        log.error( "[ ReaderM ] All Sub Thread Start : OK" );
    }

    public void stop() {
        this.readers.forEach( ReaderThread::stop );
        log.error( "[ ReaderM ] Send Stop Signal To Sub Thread : OK" );
        this.readerThreads.forEach( thread -> {
            try {
                thread.join();
            } catch( InterruptedException e ) {
                log.error( e.getMessage() );
            }
        } );
        log.error( "[ ReaderM ] All Sub Thread Stop : OK" );
    }
}
