package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.worker.Job;
import org.junit.jupiter.api.Test;

class AddStorageTaskTest {

    @Test
    void test() {
//        Job mysqlJob = Job.builder().storageId( "2b6c8550-a7f8-4c96-9d17-cd10770ace87" ).build();
//        AddStorageTask task = new AddStorageTask( mysqlJob );
//        task.run();

        Job postgresJob = Job.builder().storageId( "1b6c8550-a7f8-4c96-9d17-cd10770ace87" ).build();
        AddStorageTask task = new AddStorageTask( postgresJob );
        task.run();
    }
}