package com.mobigen.datafabric.core.job;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JobQueueImpl implements JobQueue {
    private QueueMode mode;
    private BlockingQueue<Job> queue;

    public JobQueueImpl( QueueMode mode, Integer maxQueueSize) {
        this.mode = mode;
        if( maxQueueSize == null ) {
            queue = new LinkedBlockingQueue<>();
        } else {
            queue = new LinkedBlockingQueue<>( maxQueueSize );
        }
    }

    @Override
    public QueueResult push( Job job ) throws Exception {
        if( mode.equals( QueueMode.BLOCKING )) {
            queue.put( job );
            return QueueResult.SUCCESS;
        } else {
            return queue.offer( job ) ? QueueResult.SUCCESS : QueueResult.FAIL;
        }
    }

    @Override
    public Job pop() throws Exception {
        if( mode.equals( QueueMode.BLOCKING )) {
            return queue.take();
        } else {
            return queue.poll();
        }
    }
}
