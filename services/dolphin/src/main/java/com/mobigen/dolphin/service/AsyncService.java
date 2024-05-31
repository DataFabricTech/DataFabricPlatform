package com.mobigen.dolphin.service;

import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.repository.local.JobRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@RequiredArgsConstructor
@Service
public class AsyncService {
    private final TrinoRepository trinoRepository;
    private final JobRepository jobRepository;

    @Async
    public void executeAsync(JobEntity jobEntity) {
        jobEntity.setStatus(JobEntity.JobStatus.RUNNING);
        jobRepository.save(jobEntity);
        var result = trinoRepository.executeQuery(jobEntity.getId(), jobEntity.getConvertedQuery());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        jobEntity.setResultPath(result);
        jobEntity.setStatus(JobEntity.JobStatus.FINISHED);
        jobRepository.save(jobEntity);
    }
}
