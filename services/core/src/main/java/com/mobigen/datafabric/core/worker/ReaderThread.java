package com.mobigen.datafabric.core.worker;

import com.mobigen.datafabric.core.job.Job;
import com.mobigen.datafabric.core.job.JobQueue;
import com.mobigen.datafabric.core.worker.task.AutoAddTask;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ReaderThread implements Runnable {
    private final String readerName;
    private Map<String, JobQueue> queues;
    private Boolean isRunning;
    private final Worker worker;

    public ReaderThread( String name, Worker worker ) {
        this.readerName = name;
        this.isRunning = true;
        this.queues = new LinkedHashMap<>();
        this.worker = worker;
        log.error( "[ {} ] Init : OK", name );
    }

    public void addQueue( String name, JobQueue queue ) {
        this.queues.put( name, queue );
        log.error( "[ {} ] Set Queue ID(Name) : {}", this.readerName, name );
    }

    @Override
    public void run() {
        log.error( "[ {} ] Start : OK", this.readerName );
        while( isRunning ) {
            for( Map.Entry<String, JobQueue> entry : this.queues.entrySet() ) {
                try {
                    log.error( "[ {} ] Pop Queue ID(Name) : {}", this.readerName, entry.getKey() );
                    Job job = entry.getValue().pop();
                    if( job == null ) continue;
                    if( worker == null ) {
                        log.error( "[ {} ] Worker NotSet", this.readerName );
                        continue;
                    }
                    worker.runTask( new AutoAddTask( job ) );
                } catch( Exception e ) {
                    log.error( e.getMessage() );
                }
            }
            try {
                Thread.sleep( 100 );
            } catch( InterruptedException e ) {
                throw new RuntimeException( e );
            }
        }
        log.error( "[ {} ] Stop : OK", this.readerName );
    }

    public void stop() {
        log.error( "[ {} ] Receive Stop", this.readerName );
        this.isRunning = false;
    }
}
