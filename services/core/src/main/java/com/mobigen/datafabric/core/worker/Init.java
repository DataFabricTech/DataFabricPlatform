package com.mobigen.datafabric.core.worker;

import com.mobigen.datafabric.core.job.JobQueue;
import com.mobigen.datafabric.core.job.JobQueueImpl;
import com.mobigen.datafabric.core.job.QueueMode;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class Init {

    private static Init instance = null;

    private final Reader reader;
    private final Worker worker;
    private final int readerMaxThreadCount;

    public Init(int readerMaxThreadCount, int workerMaxThreadCount) {
        this.readerMaxThreadCount = readerMaxThreadCount;
        // Set Worker
        worker = new Worker( workerMaxThreadCount );

        // Set Queue
        Map<String, JobQueue> queues = new LinkedHashMap<>();
        queues.put( "Storage", new JobQueueImpl( QueueMode.NON_BLOCKING, 100) );
        queues.put( "DataCatalog", new JobQueueImpl( QueueMode.NON_BLOCKING, 1000) );

        // Set Reader
        reader = new Reader( queues, worker );

        // Set Singleton Instance
        instance = this;

        log.error( "[ Worker ] Init : OK" );
    }

    public static Init getInstance() {
        if(instance == null) {
            log.error( "[ Worker ] Not Init" );
            return null;
        }
        return instance;
    }

    public void Start() {
        this.reader.start( readerMaxThreadCount );

        // Worker Monitoring
        // Use Timer
//        Runnable monitoring = () -> {
//            while( true ) {
//                try {
//                    Thread.sleep( 1000 );
//                } catch ( InterruptedException e ) {
//                    log.error( "[ Worker ] Monitoring Thread Sleep Error[ {} ]", e.getMessage() );
//                }
//                worker.monitoring();
//            }
//        };
//        Thread t = new Thread( monitoring );
//        t.start();
    }

    public void destroy() {
        this.reader.stop();
        this.worker.shutdown();
        log.error( "[ Worker ] Destroy : OK" );
    }

}
