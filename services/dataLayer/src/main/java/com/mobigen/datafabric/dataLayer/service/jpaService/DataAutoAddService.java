package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.DataAutoAdd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DataAutoAddService extends SyncService<DataAutoAdd, UUID> {
    public DataAutoAddService(JpaRepository<DataAutoAdd, UUID> repository) {
        super(repository);
    }
}
