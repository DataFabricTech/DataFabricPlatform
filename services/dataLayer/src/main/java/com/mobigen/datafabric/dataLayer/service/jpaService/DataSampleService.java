package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.DataSample;
import dto.compositeKeys.DataSampleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DataSampleService extends SyncService<DataSample, DataSampleKey> {
    public DataSampleService(JpaRepository<DataSample, DataSampleKey> repository) {
        super(repository);
    }
}
