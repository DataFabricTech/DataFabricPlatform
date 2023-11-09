package com.mobigen.datafabric.core.worker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WorkerTest {

    @Test
    void startAndMonitoring() throws InterruptedException {
        Worker worker = new Worker(1, 1000, 10);
        worker.start();
        Assertions.assertNotNull( worker.getQueues());
        Assertions.assertNotNull( worker.getReader());
        Assertions.assertNotNull( worker.getTimer());
        Assertions.assertNotNull( worker.getThreadPool());
        Thread.sleep(6000);
        worker.destroy();
    }

    @Test
    void insertAndTaskTest() throws Exception {
        Worker worker = new Worker(1, 1000, 10);
        worker.start();

        worker.getQueues().get("Storage").add(Job.builder()
                .type(Job.JobType.STORAGE_ADD)
                .storageId("StorageId")
                .build());

        worker.getQueues().get("DataCatalog").add(Job.builder()
                .type(Job.JobType.STORAGE_ADD)
                .dataCatalogId("DataCatalogId")
                .build());

        Thread.sleep(500);

        worker.destroy();
    }

}