package com.mobigen.datafabric.core.worker;

import com.mobigen.datafabric.core.worker.queue.Queue;
import com.mobigen.datafabric.core.worker.task.AutoAddTask;
import com.mobigen.datafabric.core.worker.timer.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ReaderThread implements Runnable {
    private final String readerName;
    private final Map<String, Queue<Job>> queues;
    private Boolean isRunning;
    private final Worker worker;
    private final Timer timer;
    private long runCnt;

    public ReaderThread(String name, Timer timer, Worker worker) {
        this.readerName = name;
        this.isRunning = true;
        this.queues = new LinkedHashMap<>();
        this.timer = timer;
        this.worker = worker;
        log.error("[ {} ] Init : OK", name);
    }

    public void addQueue(String name, Queue<Job> queue) {
        this.queues.put(name, queue);
        log.error("[ {} ] Set Queue ID(Name) : {}", this.readerName, name);
    }

    @Override
    public void run() {
        log.error("[ {} ] Start : OK", this.readerName);
        Job job = null;
        while (isRunning) {
            for (Map.Entry<String, Queue<Job>> entry : this.queues.entrySet()) {
                try {
                    var q = entry.getValue();
                    job = q.poll();
                    if (job == null) continue;
                } catch (Exception e) {
                    log.error("[ {} ] Error Queue Pool From Queue[ {} ]. Msg[ {} ]",
                            this.readerName, entry.getKey(), e.getMessage());
                    continue;
                }

                // Job Type -> Set Task -> Run Task( Worker )
                switch( job.getType() ) {
                    case STORAGE_ADD -> addStorage(job);
//                    case STORAGE_UPDATE -> updateStorage(job);
//                    case STORAGE_DELETE -> deleteStorage(job);
                    case AUTO_CREATE_DATA_CATALOG -> log.info("[ {} ] Job Type : AUTO CREATE DATA CATALOG", this.readerName);
                    case STORAGE_SYNC -> log.info("[ {} ] Job Type : CREATE DATA CATALOG", this.readerName);
                }

                // Need Timer?
                if (timer == null) {
                    log.error("[ {} ] Timer NotSet", this.readerName);
                }

                // Need Worker?
                if (worker != null) {
                    if (job.getType().equals(Job.JobType.STORAGE_ADD)) {
                        worker.runTask(new AutoAddTask(job));
                    }
                }
            }

            try {
                // Thread 실행 상태 확인 용
                runCnt = runCnt + 1;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.error("[ {} ] Stop : OK", this.readerName);
    }

    public void stop() {
        log.error("[ {} ] Receive Stop", this.readerName);
        this.isRunning = false;
    }

    public void monitoring() {
        log.error("[ {} ] Running... Count [ {} ]", this.readerName, this.runCnt);
    }

    public void addStorage(Job job) {
        log.info("[ {} ] Add Storage. ID[ {} ]", this.readerName, job.getStorageId());
        if( job.getStorageId() == null ) {
            log.error("[ {} ] Storage ID is Null", this.readerName);
            return;
        }

    }

}
