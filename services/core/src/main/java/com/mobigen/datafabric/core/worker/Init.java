package com.mobigen.datafabric.core.worker;

import com.mobigen.datafabric.core.worker.queue.Queue;
import com.mobigen.datafabric.core.worker.queue.QueueImpl;
import com.mobigen.datafabric.core.worker.queue.QueueMode;
import com.mobigen.datafabric.core.worker.timer.Timer;
import com.mobigen.datafabric.core.worker.timer.TimerCallback;
import com.mobigen.datafabric.core.worker.timer.TimerData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Getter
public class Init {
    private static Init instance = null;
    private final Reader reader;
    private final Worker worker;
    private final int readerMaxThreadCount;
    private final Timer timer;
    private final Map<String, Queue<Job>> queues;

    public final static String STORAGE_QUEUE = "Storage";
    public final static String DATA_CATALOG_QUEUE = "DataCatalog";

    public Init(int readerMaxThreadCount, int timerMaxCount, int workerMaxThreadCount) {
        this.readerMaxThreadCount = readerMaxThreadCount;
        // Set Worker
        worker = new Worker(workerMaxThreadCount);
        // Set Timer
        timer = new Timer(timerMaxCount, worker);
        // Set Queue
        queues = new LinkedHashMap<>();
        queues.put(STORAGE_QUEUE, new QueueImpl<>(QueueMode.NON_BLOCKING, 100));
        queues.put(DATA_CATALOG_QUEUE, new QueueImpl<>(QueueMode.NON_BLOCKING, 1000));

        // Set Reader
        reader = new Reader(queues, timer, worker);

        // Set Singleton Instance
        instance = this;

        log.error("[ Worker ] Init : OK");
    }

    public static Init getInstance() {
        if (instance == null) {
            log.error("[ Worker ] Not Init");
            return null;
        }
        return instance;
    }

    public void start() {
        this.reader.start(readerMaxThreadCount);
        Runnable r = this.timer::Start;
        Thread t = new Thread(r);
        t.start();

        // Monitoring
        this.timer.Add(null, 5000L, true, new Monitoring());
    }

    public void destroy() {
        this.reader.stop();
        this.worker.shutdown();
        log.error("[ Worker ] Destroy : OK");
    }

    public static class Monitoring implements TimerCallback {
        @Override
        public void callback(TimerData timerData) {
            Init it = Init.getInstance();
            if (it != null) {
                log.error("[ Worker ]============================= Monitoring =============================");
                it.reader.monitoring();
                it.worker.monitoring();
                it.timer.monitoring();
                log.error("[ Worker ]============================= Monitoring =============================");
            }
        }
    }
}
