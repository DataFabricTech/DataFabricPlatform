package com.mobigen.datafabric.core.worker.task;

import com.mobigen.datafabric.core.worker.Job;
import org.junit.jupiter.api.Test;

class AddStorageTaskTest {

    @Test
    void test() {
        AddStorageTask task = new AddStorageTask(Job.builder().type(Job.JobType.STORAGE_ADD).build());
        task.run();
    }

}