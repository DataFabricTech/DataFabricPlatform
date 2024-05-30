package com.mobigen.dolphin.service;

import com.mobigen.dolphin.entity.local.JobEntity;
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

    @Async
    public void executeAsync(JobEntity jobEntity) {
        trinoRepository.executeQuery(jobEntity.getId(), jobEntity.getConvertedQuery());
    }
}
