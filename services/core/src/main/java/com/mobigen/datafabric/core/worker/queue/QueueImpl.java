package com.mobigen.datafabric.core.worker.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueImpl<E> implements Queue<E> {
    private final QueueMode mode;
    private final BlockingQueue<E> queue;

    public QueueImpl(QueueMode mode, Integer maxQueueSize) {
        this.mode = mode;
        if( maxQueueSize == null ) {
            queue = new LinkedBlockingQueue<E>();
        } else {
            queue = new LinkedBlockingQueue<E>( maxQueueSize );
        }
    }

    @Override
    public boolean add(E e) throws Exception {
        if( mode.equals( QueueMode.BLOCKING )) {
            queue.put( e );
            return true;
        } else {
            return queue.offer( e );
        }
    }

    @Override
    public E poll() throws Exception {
        // Non-Blocking
        if( mode.equals( QueueMode.NON_BLOCKING )) return queue.poll();
        // Blocking
        return queue.take();
    }
}
