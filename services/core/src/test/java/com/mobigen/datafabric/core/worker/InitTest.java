package com.mobigen.datafabric.core.worker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InitTest {

    @Test
    void startAndMonitoring() throws InterruptedException {
        Init init = new Init(1, 1000, 10);
        init.start();
        Assertions.assertNotNull(init.getQueues());
        Assertions.assertNotNull(init.getReader());
        Assertions.assertNotNull(init.getTimer());
        Assertions.assertNotNull(init.getWorker());
        Thread.sleep(6000);
        init.destroy();
    }

    @Test
    void insertAndTaskTest() throws Exception {
        Init init = new Init(1, 1000, 10);
        init.start();

        init.getQueues().get("Storage").add(Job.builder()
                .type(Job.JobType.STORAGE_ADD)
                .storageId("StorageId")
                .build());

        init.getQueues().get("DataCatalog").add(Job.builder()
                .type(Job.JobType.STORAGE_ADD)
                .dataCatalogId("DataCatalogId")
                .build());

        Thread.sleep(500);

        init.destroy();
    }

}