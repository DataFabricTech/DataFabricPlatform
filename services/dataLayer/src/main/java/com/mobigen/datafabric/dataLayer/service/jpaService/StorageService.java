package com.mobigen.datafabric.dataLayer.service.jpaService;

import com.mobigen.datafabric.dataLayer.service.SyncService;
import dto.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StorageService extends SyncService<Storage, UUID> {
    public StorageService(JpaRepository<Storage, UUID> repository) {
        super(repository);
    }
}
